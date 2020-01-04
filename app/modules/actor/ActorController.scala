package modules.actor

import java.time.LocalDate

import javax.inject._
import modules.util._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.{max, min}
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents, _}
import views.html

import scala.concurrent.{ExecutionContext, Future}

class ActorController @Inject()(
    repo: ActorRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  val actorForm: Form[ActorForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "dateOfBirth" -> localDate,
      "nationality" -> of(CountryFormFormatter),
      "height" -> number.verifying(min(100), max(240)),
      "gender" -> of(GenderFormatter)

    )(ActorForm.apply)(ActorForm.unapply)
  }
  val actorFilterForm: Form[ActorFilterForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "dateOfBirth" -> optional(localDate),
      "nationality" -> of(CountryFormFormatter),
      "heightMin" -> number,
      "heightMax" -> number,
      "gender" -> of(GenderFormatter)
    )(ActorFilterForm.apply)(ActorFilterForm.unapply)
  }
  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list (
      firstName: String,
      lastName:String,
      dateOfBirth: String,
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value) = Action.async { implicit request =>
    val firstNameOp = if (firstName.trim().nonEmpty) Some(firstName) else None
    val lastNameOp = if (lastName.trim().nonEmpty) Some(lastName) else None
    repo.list(
      firstNameOp,
      lastNameOp,
      parseDate(dateOfBirth),
      nationality,
      heightMin,
      heightMax,
      gender,
      orderBy,
      order

    ).map(actors => Ok(html.actor.list(
      actors,
      actorFilterForm.fill(
        ActorFilterForm(firstName, lastName, parseDate(dateOfBirth), nationality, heightMin, heightMax, gender)),
      SortItems(orderBy, order)
    )))
  }

  def createView = Action { implicit request =>
    Ok(html.actor.create(actorForm))
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    actorForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(html.actor.create(errorForm)))
      },
      actor => {
        repo.create(actor).map { _ =>
          Redirect(routes.ActorController.list()).flashing("success" -> "actor.created")
        }
      }
    )
  }
  def edit(id:Long)= Action.async{implicit request =>
    val actor= repo.findById(id)
    actor.map { actor =>
      actor match {
        case Some(a)=> Ok(html.actor.edit(a,actorForm.fill(ActorForm(a.firstName,a.lastName, a.dateOfBirth, a.nationality, a.height, a.gender))))
        case None => NotFound
      }
    }
  }
  def update(id:Long) = Action.async { implicit request =>

    val futActor = repo.findById(id)
    actorForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          for {
            actorOP <- repo.findById(id)
          } yield Ok(html.actor.edit(actorOP.get, formWithErrors)),
        editActor =>
          for {
            actorOP <- repo.findById(id)
            update <- repo.update(ActorExtendedForm(
              id,
              editActor.firstName,
              editActor.lastName,
              editActor.dateOfBirth,
              editActor.nationality,
              editActor.height,
              editActor.gender ))
          } yield Redirect(routes.ActorController.list()))
  }
  def delete(id: Long) = Action.async { implicit rs =>
    for {
      _ <- repo.delete(id)
    } yield Redirect(routes.ActorController.list())
  }
}

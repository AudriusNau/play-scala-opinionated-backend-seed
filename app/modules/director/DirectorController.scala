package modules.director

import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.{max, min}
import play.api.mvc._
import views.html
import modules.util._

import scala.concurrent.{ExecutionContext, Future}

class DirectorController @Inject()(
    repo: DirectorRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  val directorForm: Form[DirectorForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "dateOfBirth" -> localDate,
      "nationality" -> of(CountryFormFormatter),
      "height" -> number.verifying(min(100), max(240)),
      "gender" -> of(GenderFormatter)

    )(DirectorForm.apply)(DirectorForm.unapply)
  }
  val directorFilterForm: Form[DirectorFilterForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "dateOfBirth" -> optional(localDate),
      "nationality" -> of(CountryFormFormatter),
      "heightMin" -> number,
      "heightMax" -> number,
      "gender" -> of(GenderFormatter)
    )(DirectorFilterForm.apply)(DirectorFilterForm.unapply)
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

    ).map(directors => Ok(html.director.list(
      directors,
      directorFilterForm.fill(
        DirectorFilterForm(firstName, lastName, parseDate(dateOfBirth), nationality, heightMin, heightMax, gender)),
      SortItems(orderBy, order)
    )))
  }

  def createView = Action { implicit request =>
    Ok(html.director.create(directorForm))
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    directorForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(html.director.create(errorForm)))
      },
      director => {
        repo.create(director).map { _ =>
          Redirect(routes.DirectorController.list()).flashing("success" -> "director.created")
        }
      }
    )
  }
  def edit(id:Long)= Action.async{implicit request =>
    val director= repo.findById(id)
    director.map { director =>
      director match {
        case Some(a)=> Ok(html.director.edit(a,directorForm.fill(DirectorForm(a.firstName,a.lastName, a.dateOfBirth, a.nationality, a.height, a.gender))))
        case None => NotFound
      }
    }
  }
  def update(id:Long) = Action.async { implicit request =>

    val futDirector = repo.findById(id)
    directorForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          for {
            directorOP <- repo.findById(id)
          } yield Ok(html.director.edit(directorOP.get, formWithErrors)),
        editDirector =>
          for {
            directorOP <- repo.findById(id)
            update <- repo.update(DirectorExtendedForm(
              id,
              editDirector.firstName,
              editDirector.lastName,
              editDirector.dateOfBirth,
              editDirector.nationality,
              editDirector.height,
              editDirector.gender ))
          } yield Redirect(routes.DirectorController.list()))
  }
  def delete(id: Long) = Action.async { implicit rs =>
    for {
      _ <- repo.delete(id)
    } yield Redirect(routes.DirectorController.list())
  }
}

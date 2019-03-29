package modules.actor

import javax.inject._
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
  val actorForm: Form[CreateActorForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "dateOfBirth" -> localDate,
      "nationality" -> nonEmptyText,
      "height" -> number.verifying(min(100), max(240)),
      "gender" -> nonEmptyText

    )(CreateActorForm.apply)(CreateActorForm.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list = Action.async { implicit request =>
    repo.list().map(actors => Ok(html.actor.list(actors)))
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
        repo.create(actor.firstName, actor.lastName,actor.dateOfBirth, actor.nationality,actor.height, actor.gender).map { _ =>
          Redirect(routes.ActorController.list()).flashing("success" -> "actor.created")
        }
      }
    )
  }
}

package modules.director



import java.time.LocalDate

import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.{max, min}
import play.api.mvc._
import views.html

import scala.concurrent.{ExecutionContext, Future}

class DirectorController @Inject()(
    repo: DirectorRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  val directorForm: Form[CreateDirectorForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "dateOfBirth" -> localDate,
      "nationality" -> nonEmptyText,
      "height" -> number.verifying(min(100), max(240)),
      "gender" -> nonEmptyText

    )(CreateDirectorForm.apply)(CreateDirectorForm.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list = Action.async { implicit request =>
    repo.list().map(directors => Ok(html.director.list(directors)))
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
        repo.create(director.firstName, director.lastName,director.dateOfBirth, director.nationality,director.height, director.gender).map { _ =>
          Redirect(routes.DirectorController.list()).flashing("success" -> "director.created")
        }
      }
    )
  }
}

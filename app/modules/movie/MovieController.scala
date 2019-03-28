package modules.movie


import java.time
import java.time.LocalDate

import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import views.html

import scala.concurrent.{ExecutionContext, Future}
class MovieController @Inject()(
    repo: MovieRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val movieForm: Form[CreateMovieForm] = Form {
    mapping(
      "title" -> nonEmptyText,
     "description" -> nonEmptyText,
      "releaseDate" -> localDate,
      "country" -> nonEmptyText,
      "language" -> nonEmptyText

    )(CreateMovieForm.apply)(CreateMovieForm.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list = Action.async { implicit request =>
    repo.list().map(movies => Ok(html.movie.list(movies)))
  }

  def createView = Action { implicit request =>
    Ok(html.movie.create(movieForm))
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    movieForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(html.movie.create(errorForm)))
      },
      movie => {
        repo.create(movie.title, movie.description,movie.releaseDate, movie.country, movie.language).map { _ =>
          Redirect(routes.MovieController.list()).flashing("success" -> "movie.created")
        }
      }
    )
  }
}

package modules.movie


import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import javax.inject._
import modules.util._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import views.html

import scala.concurrent.{ExecutionContext, Future}
class MovieController @Inject()(
    repo: MovieRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val movieForm: Form[MovieForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "releaseDate" -> localDate,
      "country" -> of(CountryFormFormatter),
      "language" -> of(LanguageFormatter)

    )(MovieForm.apply)(MovieForm.unapply)
  }
  val filterMovieForm: Form[FilterMovieForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "releaseDate" -> optional(localDate),
      "country" -> of(CountryFormFormatter),
      "language" -> of(LanguageFormatter)
    )(FilterMovieForm.apply)(FilterMovieForm.unapply)
  }


  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list( title: String,
      description: String,
      releaseDate: String,
      country: Country.Value,
      language: Language.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value)  = Action.async { implicit request =>
    val titleOptioned = if (title.trim().nonEmpty) Some(title) else None
    val descriptionOptioned = if (description.trim().nonEmpty) Some(description) else None
    repo.list(
      titleOptioned,
      descriptionOptioned,
      parseDate(releaseDate),
      country,
      language,
      orderBy,
      order
    )
      .map(movies =>
        Ok(html.movie.list(
          movies,
          filterMovieForm.fill(
            FilterMovieForm(title, description, parseDate(releaseDate), country, language)
          ),
          SortItems(orderBy, order)
        ))
      )

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
        repo.create(movie).map { _ =>
          Redirect(routes.MovieController.list()).flashing("success" -> "movie.created")
        }
      }
    )
  }
  def edit(id:Long)= Action.async{implicit request =>
    val movie= repo.findById(id)
    movie.map { movie =>
      movie match {
        case Some(a)=> Ok(html.movie.edit(a,movieForm.fill(MovieForm(a.title,a.description, a.releaseDate, a.country, a.language))))
        case None => NotFound
      }
    }
  }
  def update(id:Long) = Action.async { implicit request =>

    val futMovie = repo.findById(id)
    movieForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          for {
            movieOP <- repo.findById(id)
          } yield Ok(html.movie.edit(movieOP.get, formWithErrors)),
        editMovie =>
          for {
            movieOP <- repo.findById(id)
            update <- repo.update(MovieExtendedForm(
              id,
              editMovie.title,
              editMovie.description,
              editMovie.releaseDate,
              editMovie.country,
              editMovie.language))
          } yield Redirect(routes.MovieController.list()))
  }
  def delete(id: Long) = Action.async { implicit rs =>
    for {
      _ <- repo.delete(id)
    } yield Redirect(routes.MovieController.list())
  }
}

package modules.genre

import javax.inject.Inject
import modules.util.SortOrder
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import views.html

import scala.concurrent.{ExecutionContext, Future}

class GenreController @Inject()(
    repo: GenreRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  val genreForm: Form[Genre] = Form {
    mapping(
      "title" -> nonEmptyText
    )(Genre.apply)(Genre.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list(
      title: String,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = Action.async { implicit request =>
    val titleMod = if (title.trim.nonEmpty) Some(title) else None
    repo
      .list(titleMod, orderBy, order)
      .map(genres =>
        Ok(html.genre.list(genres,
          genreForm.fill(Genre(title)),
          SortItems(orderBy, order))))
  }

  def createView = Action { implicit request =>
    Ok(html.genre.create(genreForm))
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    genreForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(html.genre.create(errorForm)))
      },
      genre => {
        repo.create(genre.title).map { _ =>
          Redirect(routes.GenreController.list()).flashing("success" -> "Genre created")
        }
      }
    )
  }
  def edit(id:Long)= Action.async{implicit request =>
    val genre= repo.findById(id)
    genre.map { genre =>
      genre match {
        case Some(a)=> Ok(html.genre.edit(a,genreForm.fill(Genre(a.title))))
        case None => NotFound
      }
    }
  }
  def update(id:Long) = Action.async { implicit request =>

    val futGenre = repo.findById(id)
    genreForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          for {
            genreOP <- repo.findById(id)
          } yield Ok(html.genre.edit(genreOP.get, formWithErrors)),
        editGenre =>
          for {
            genreOP <- repo.findById(id)
            update <- repo.update(GenreExtended(
              id,
              editGenre.title))
          } yield Redirect(routes.GenreController.list()))
  }
  def delete(id: Long) = Action.async { implicit rs =>
    for {
      _ <- repo.delete(id)
    } yield Redirect(routes.GenreController.list())
  }
}

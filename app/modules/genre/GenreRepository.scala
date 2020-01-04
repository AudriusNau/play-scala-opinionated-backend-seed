package modules.genre

import javax.inject.{Inject, Singleton}
import modules.util.SortOrder
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted.ColumnOrdered

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GenreRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
)extends GenreTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def list(
      title:Option[String],
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Seq[GenreExtended]] =  {
    val filteredQuery = filter(title)
    val sortedQuery = filteredQuery.sortBy{ m => sort(m, orderBy, order) }
    db.run {sortedQuery.result}
  }

  def find(id: Long): Future[Option[GenreExtended]] = {
    db.run(genres.filter(_.id === id).result.headOption)
  }

  def create(title: String): Future[GenreExtended] = db.run {
    (genres.map(p => p.title)
      returning genres.map(_.id)
      into ((title, id) => GenreExtended(id, title))
      ) += title
  }

  def save(genre: GenreExtended): Future[GenreExtended] = {
    db.run((genres returning genres).insertOrUpdate(genre))
      .map {
        case Some(newGenre) =>
          logger.debug(s"Created new genre $newGenre")
          newGenre
        case _ =>
          logger.debug(s"Updated existing genre $genre")
          genre
      }
  }
  def findById(id: Long): Future[Option[GenreExtended]] =
    db.run(genres.filter(_.id === id).result.headOption)

  def update(genre: GenreExtended): Future[Int] = {
    val updated = genres
      .insertOrUpdate(GenreExtended(
        genre.id,
        genre.title
      )
      )
    db.run(updated)
  }
  def delete(id: Long): Future[Unit] = {
    db.run(genres.filter(_.id === id).delete).map(_ => ())
  }
  private def filter(title: Option[String]) = {
    title.map {
      titleVal =>
        genres.filter(genre => genre.title like "%" + titleVal + "%")
    }.getOrElse(genres)
  }

  private def sort(
      genreTable: GenreTable,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    val ordering = if (order == SortOrder.desc) slick.ast.Ordering.Desc else slick.ast.Ordering.Asc
    orderBy match {
      case SortableField.title => ColumnOrdered(genreTable.title.toLowerCase, slick.ast.Ordering(ordering))
      case _ => genreTable.id.asc
    }
  }
}

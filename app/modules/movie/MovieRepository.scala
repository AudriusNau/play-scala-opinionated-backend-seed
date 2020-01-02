package modules.movie

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import modules.util._
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted.ColumnOrdered

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovieRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends MovieTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)


  def list(
      title: Option[String],
      description: Option[String],
      releaseDate: Option[LocalDate],
      country: Country.Value,
      language: Language.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Seq[MovieExtendedForm]] = {
    val filteredQuery = filter(title, description, releaseDate, country, language)
    val sortedQuery = filteredQuery.sortBy{ m => sort(m, orderBy, order) }

     val movieList = db.run {
        sortedQuery
          .result
      }
    movieList
  }


  def find(id: Long): Future[Option[MovieExtendedForm]] = {
    db.run(movies.filter(_.id === id).result.headOption)
  }

  def create(movieForm: MovieForm): Future[MovieExtendedForm] = db.run {
    (movies
      .map(p =>
        (
          p.title,
          p.description,
          p.releaseDate,
          p.country,
          p.language
        ))
      returning movies
      into ((_, id) =>
      id)) +=
      (
        movieForm.title,
        movieForm.description,
        movieForm.releaseDate,
        movieForm.country,
        movieForm.language
      )
  }


  def save(movie: MovieExtendedForm): Future[MovieExtendedForm] = {
    db.run((movies returning movies).insertOrUpdate(movie))
      .map {
        case Some(newMovie) =>
          logger.debug(s"Created new movie $newMovie")
          newMovie
        case _ =>
          logger.debug(s"Updated existing movie $movie")
          movie
      }
  }

  def findById(id: Long): Future[Option[MovieExtendedForm]] =
    db.run(movies.filter(_.id === id).result.headOption)

  def update(movie: MovieExtendedForm): Future[Int] = {
    val updated = movies
      .insertOrUpdate(MovieExtendedForm(
        movie.id,
        movie.title,
        movie.description,
        movie.releaseDate,
        movie.country,
        movie.language
      )
      )
    db.run(updated)
  }
  def delete(id: Long): Future[Unit] = {
    db.run(movies.filter(_.id === id).delete).map(_ => ())
  }

  private def filter(
      title: Option[String],
      description: Option[String],
      releaseDate: Option[LocalDate],
      country: Country.Value,
      language: Language.Value
  ) = {
    val titles = title.map {
      titleVal =>
        movies.filter(movie => movie.title like "%" + titleVal + "%")
    }.getOrElse(movies)
    val descriptions = description.map {
      descriptionVal =>
        titles.filter(movie => movie.description like "%" + descriptionVal + "%")
    }.getOrElse(titles)
    val dates = releaseDate match {
      case Some(date) => descriptions.filter(movie => movie.releaseDate === date)
      case None => descriptions
    }
    val countries = country match {
      case Country.NoCountry => dates
      case _ => dates.filter(movie => movie.country === country)
    }
    val languages = language match {
      case Language.NoLanguage => countries
      case _ => countries.filter(movie => movie.language === language)
    }
    languages
  }
  private def sort(
      movieTable: MovieTable,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    val ordering = if (order == SortOrder.desc) slick.ast.Ordering.Desc else slick.ast.Ordering.Asc
    orderBy match {
      case SortableField.title => ColumnOrdered(movieTable.title.toLowerCase, slick.ast.Ordering(ordering))
      case SortableField.description => ColumnOrdered(movieTable.description.toLowerCase, slick.ast.Ordering(ordering))
      case SortableField.releaseDate => ColumnOrdered(movieTable.releaseDate, slick.ast.Ordering(ordering))
      case SortableField.country => ColumnOrdered(movieTable.country, slick.ast.Ordering(ordering))
      case SortableField.language => ColumnOrdered(movieTable.language, slick.ast.Ordering(ordering))
      case _ => movieTable.id.asc
    }
  }
}

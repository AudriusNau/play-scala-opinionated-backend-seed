package modules.movie

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import javax.inject.{Inject, Singleton}
import modules.movie
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class MovieRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends MovieTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def list(): Future[Seq[Movie]] = db.run {
    movies.result
  }


  def find(id: Long): Future[Option[Movie]] = {
    db.run(movies.filter(_.id === id).result.headOption)
  }

  def create(title: String, description: String,releaseDate: LocalDate, country: String, language: String): Future[Movie] = db.run {
    (movies.map(p => (p.title, p.description, p.releaseDate,p.country, p.language))
      returning movies.map(_.id)
      into ((movie, id) => Movie(id, movie._1, movie._2,movie._3,movie._4,movie._5))
      ) += (title, description,releaseDate,country,language)
  }

  def save(movie: Movie): Future[Movie] = {
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
}

package modules.director

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import javax.inject.{Inject, Singleton}
import modules.director
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class DirectorRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends DirectorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def list(): Future[Seq[Director]] = db.run {
    directors.result
  }


  def find(id: Long): Future[Option[Director]] = {
    db.run(directors.filter(_.id === id).result.headOption)
  }

  def create(firstName: String, lastName: String,
      dateOfBirth: LocalDate, nationality: String,
      height:Int, gender: String): Future[Director] =
    db.run {
    (directors.map(p => (p.firstName, p.lastName, p.dateOfBirth,p.nationality, p.height,p.gender))
      returning directors.map(_.id)
      into ((director, id) => Director(id, director._1, director._2,director._3,director._4,director._5,director._6))
      ) += (firstName, lastName,dateOfBirth,nationality,height,gender)
  }

  def save(director: Director): Future[Director] = {
    db.run((directors returning directors).insertOrUpdate(director))
      .map {
        case Some(newDirector) =>
          logger.debug(s"Created new director $newDirector")
          newDirector
        case _ =>
          logger.debug(s"Updated existing director $director")
          director
      }
  }
}

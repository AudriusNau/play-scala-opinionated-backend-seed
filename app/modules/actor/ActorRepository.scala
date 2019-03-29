package modules.actor

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActorRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends ActorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def list(): Future[Seq[Actor]] = db.run {
    actors.result
  }


  def find(id: Long): Future[Option[Actor]] = {
    db.run(actors.filter(_.id === id).result.headOption)
  }

  def create(firstName: String, lastName: String,
      dateOfBirth: LocalDate, nationality: String,
      height:Int, gender: String): Future[Actor] =
    db.run {
    (actors.map(p => (p.firstName, p.lastName, p.dateOfBirth,p.nationality, p.height,p.gender))
      returning actors.map(_.id)
      into ((actor, id) => Actor(id, actor._1, actor._2,actor._3,actor._4,actor._5,actor._6))
      ) += (firstName, lastName,dateOfBirth,nationality,height,gender)
  }

  def save(actor: Actor): Future[Actor] = {
    db.run((actors returning actors).insertOrUpdate(actor))
      .map {
        case Some(newActor) =>
          logger.debug(s"Created new actor $newActor")
          newActor
        case _ =>
          logger.debug(s"Updated existing actor $actor")
          actor
      }
  }
}

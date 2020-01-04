package modules.actor

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import modules.util._
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted.ColumnOrdered

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActorRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends ActorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {

  import profile.api._

  private val logger = Logger(this.getClass)

  def list(
      firstName:Option[String],
      lastName:Option[String],
      dateOfBirth: Option[LocalDate],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ): Future[Seq[ActorExtendedForm]] = {
    val filteredQuery = filter(firstName, lastName, dateOfBirth, nationality, heightMin,heightMax,gender)
    val sortedQuery = filteredQuery.sortBy{ m => sort(m, orderBy, order) }
    db.run {sortedQuery.result}
  }


  def find(id: Long): Future[Option[ActorExtendedForm]] = {
    db.run(actors.filter(_.id === id).result.headOption)
  }

  def create(
      newActor: ActorForm
  ): Future[ActorExtendedForm] = db.run {
    (actors.map(a => (
      a.firstName,
      a.lastName,
      a.dateOfBirth,
      a.nationality,
      a.height,
      a.gender
    )) returning actors
      into ((_, actor) =>
      actor)) +=
      (
        newActor.firstName,
        newActor.lastName,
        newActor.dateOfBirth,
        newActor.nationality,
        newActor.height,
        newActor.gender)
    }

  def save(actor: ActorExtendedForm): Future[ActorExtendedForm] = {
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

  def findById(id: Long): Future[Option[ActorExtendedForm]] =
    db.run(actors.filter(_.id === id).result.headOption)

  def update(actor: ActorExtendedForm): Future[Int] = {
    val updated = actors
      .insertOrUpdate(ActorExtendedForm(
        actor.id,
        actor.firstName,
        actor.lastName,
        actor.dateOfBirth,
        actor.nationality,
        actor.height,
        actor.gender
      )
      )
    db.run(updated)
  }
  def delete(id: Long): Future[Unit] = {
    db.run(actors.filter(_.id === id).delete).map(_ => ())
  }
  private def filter(
      firstName: Option[String],
      lastName:Option[String],
      dateOfBirth: Option[LocalDate],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value
  ) = {

    val firstNames = firstName.map {
      nameVal =>
        actors.filter(actors => actors.firstName.toLowerCase like "%" + nameVal.toLowerCase + "%")
    }.getOrElse(actors).filter(actor => actor.height >= heightMin && actor.height <= heightMax)
    val lastNames = lastName.map {
      nameVal =>
        firstNames.filter(actors => actors.lastName.toLowerCase like "%" + nameVal.toLowerCase + "%")
    }.getOrElse(firstNames)
    val dateFilteredQuery = dateOfBirth match {
      case Some(date) => lastNames.filter(actor => actor.dateOfBirth === date)
      case None => lastNames
    }
    val nationalityFilter = nationality match {
      case Country.NoCountry => dateFilteredQuery
      case _ => dateFilteredQuery.filter(actor => actor.nationality === nationality)
    }
    val genderFilter = gender match {
      case Gender.Other => nationalityFilter
      case _ => nationalityFilter.filter(actor => actor.gender === gender)
    }
    genderFilter
  }
  private def sort(
      actorTable: ActorTable,
      orderBy: SortableField.Value,
      order: SortOrder.Value
  ) = {
    val ordering = if (order == SortOrder.desc) slick.ast.Ordering.Desc else slick.ast.Ordering.Asc
    orderBy match {
      case SortableField.firstName => ColumnOrdered(actorTable.firstName.toLowerCase, slick.ast.Ordering(ordering))
      case SortableField.lastName => ColumnOrdered(actorTable.lastName.toLowerCase, slick.ast.Ordering(ordering))
      case SortableField.dateOfBirth => ColumnOrdered(actorTable.dateOfBirth, slick.ast.Ordering(ordering))
      case SortableField.nationality => ColumnOrdered(actorTable.nationality, slick.ast.Ordering(ordering))
      case SortableField.height => ColumnOrdered(actorTable.height, slick.ast.Ordering(ordering))
      case _ => actorTable.id.asc
    }
  }
}
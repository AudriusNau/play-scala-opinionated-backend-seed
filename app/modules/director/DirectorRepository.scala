package modules.director

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import javax.inject.{Inject, Singleton}
import modules.util.{Country, Gender, SortOrder}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted.ColumnOrdered

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class DirectorRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends DirectorTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
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
  ): Future[Seq[DirectorExtendedForm]] = {
    val filteredQuery = filter(firstName, lastName, dateOfBirth, nationality, heightMin,heightMax,gender)
    val sortedQuery = filteredQuery.sortBy{ m => sort(m, orderBy, order) }
    db.run {sortedQuery.result}
  }


  def find(id: Long): Future[Option[DirectorExtendedForm]] = {
    db.run(directors.filter(_.id === id).result.headOption)
  }

  def create(
      newDirector: DirectorForm
  ): Future[DirectorExtendedForm] =
    db.run {
      (directors.map(d => (
        d.firstName,
        d.lastName,
        d.dateOfBirth,
        d.nationality,
        d.height,
        d.gender
      )) returning directors
        into ((_, director) =>
        director)) +=
        (
          newDirector.firstName,
          newDirector.lastName,
          newDirector.dateOfBirth,
          newDirector.nationality,
          newDirector.height,
          newDirector.gender)
    }

  def save(director: DirectorExtendedForm): Future[DirectorExtendedForm] = {
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
  def findById(id: Long): Future[Option[DirectorExtendedForm]] =
    db.run(directors.filter(_.id === id).result.headOption)

  def update(director: DirectorExtendedForm): Future[Int] = {
    val updated = directors
      .insertOrUpdate(DirectorExtendedForm(
        director.id,
        director.firstName,
        director.lastName,
        director.dateOfBirth,
        director.nationality,
        director.height,
        director.gender
      )
      )
    db.run(updated)
  }
  def delete(id: Long): Future[Unit] = {
    db.run(directors.filter(_.id === id).delete).map(_ => ())
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
        directors.filter(director => director.firstName.toLowerCase like "%" + nameVal.toLowerCase + "%")
    }.getOrElse(directors).filter(director => director.height >= heightMin && director.height <= heightMax)
    val lastNames = lastName.map {
      nameVal =>
        firstNames.filter(director => director.lastName.toLowerCase like "%" + nameVal.toLowerCase + "%")
    }.getOrElse(firstNames)
    val dateFilteredQuery = dateOfBirth match {
      case Some(date) => lastNames.filter(director => director.dateOfBirth === date)
      case None => lastNames
    }
    val nationalityFilter = nationality match {
      case Country.NoCountry => dateFilteredQuery
      case _ => dateFilteredQuery.filter(director => director.nationality === nationality)
    }
    val genderFilter = gender match {
      case Gender.Other => nationalityFilter
      case _ => nationalityFilter.filter(director => director.gender === gender)
    }
    genderFilter
  }
  private def sort(
      actorTable: DirectorTable,
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

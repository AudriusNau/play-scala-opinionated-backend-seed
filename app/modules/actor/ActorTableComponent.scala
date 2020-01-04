package modules.actor

import java.time.LocalDate

import modules.util._
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig


trait ActorTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._
  implicit val nationalityColumnType: BaseColumnType[Country.Value] = MappedColumnType.base[Country.Value, String](
    { enum => enum.dbName },
    { string => Country.findByString(string).getOrElse(Country.NoCountry) }
  )

  implicit val genderColumnType: BaseColumnType[Gender.Value] = MappedColumnType.base[Gender.Value, String](
    { enum => enum.dbName },
    { string => Gender.findByString(string).getOrElse(Gender.Other) }
  )
  class ActorTable(tag: Tag) extends Table[ActorExtendedForm](tag, "actors") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def dateOfBirth = column[LocalDate]("dateOfBirth")
    def nationality= column[Country.Value]("nationality")
    def height= column[Int]("height")
    def gender= column[Gender.Value]("gender")

    def * = (id, firstName, lastName,dateOfBirth,nationality,height,gender) <>
      ((ActorExtendedForm.apply _).tupled, ActorExtendedForm.unapply)
  }

  val actors = TableQuery[ActorTable]
}

package modules.director


import java.time.LocalDate

import modules.util.{Country, Gender}
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig


trait DirectorTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  implicit val nationalityColumnType: BaseColumnType[Country.Value] = MappedColumnType.base[Country.Value, String](
    { enum => enum.dbName },
    { string => Country.findByString(string).getOrElse(Country.NoCountry) }
  )

  implicit val genderColumnType: BaseColumnType[Gender.Value] = MappedColumnType.base[Gender.Value, String](
    { enum => enum.dbName },
    { string => Gender.findByString(string).getOrElse(Gender.Other) }
  )
  class DirectorTable(tag: Tag) extends Table[DirectorExtendedForm](tag, "directors") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def dateOfBirth = column[LocalDate]("dateOfBirth")
    def nationality= column[Country.Value]("nationality")
    def height= column[Int]("height")
    def gender= column[Gender.Value]("gender")

    def * = (id, firstName, lastName,dateOfBirth,nationality,height,gender) <> ((DirectorExtendedForm.apply _).tupled, DirectorExtendedForm.unapply)
  }

  val directors = TableQuery[DirectorTable]
}

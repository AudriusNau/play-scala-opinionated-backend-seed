package modules.director


import java.time.LocalDate

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig


trait DirectorTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class DirectorTable(tag: Tag) extends Table[Director](tag, "directors") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def dateOfBirth = column[LocalDate]("dateOfBirth")
    def nationality= column[String]("nationality")
    def height= column[Int]("height")
    def gender= column[String]("gender")

    def * = (id, firstName, lastName,dateOfBirth,nationality,height,gender) <> ((Director.apply _).tupled, Director.unapply)
  }

  val directors = TableQuery[DirectorTable]
}

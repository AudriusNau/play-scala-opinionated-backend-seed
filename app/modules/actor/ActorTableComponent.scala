package modules.actor

import java.time.LocalDate

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig


trait ActorTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class ActorTable(tag: Tag) extends Table[Actor](tag, "actors") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def dateOfBirth = column[LocalDate]("dateOfBirth")
    def nationality= column[String]("nationality")
    def height= column[Int]("height")
    def gender= column[String]("gender")

    def * = (id, firstName, lastName,dateOfBirth,nationality,height,gender) <> ((Actor.apply _).tupled, Actor.unapply)
  }

  val actors = TableQuery[ActorTable]
}

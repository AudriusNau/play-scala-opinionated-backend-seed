package modules.movie


import java.time.LocalDate

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig


trait MovieTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class MovieTable(tag: Tag) extends Table[Movie](tag, "movies") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[String]("description")
    def releaseDate = column[LocalDate]("releaseDate")
    def country= column[String]("country")
    def language= column[String]("language")

    def * = (id, title, description,releaseDate,country,language) <> ((Movie.apply _).tupled, Movie.unapply)
  }

  val movies = TableQuery[MovieTable]
}

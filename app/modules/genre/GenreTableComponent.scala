package modules.genre

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait GenreTableComponent {self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._
  class GenreTable(tag: Tag) extends Table[GenreExtended](tag, "genres") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")


    def * = (id, title) <> ((GenreExtended.apply _).tupled, GenreExtended.unapply)
  }

  val genres = TableQuery[GenreTable]

}

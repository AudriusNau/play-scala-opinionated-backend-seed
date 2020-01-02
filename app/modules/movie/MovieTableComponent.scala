package modules.movie


import java.time.LocalDate

import modules.util._
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig


trait MovieTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  implicit val countryColumnType: BaseColumnType[Country.Value] = MappedColumnType.base[Country.Value, String](
    { enum => enum.dbName },
    { string => Country.findByString(string).getOrElse(Country.NoCountry) }
  )
  implicit val languageColumnType: BaseColumnType[Language.Value] = MappedColumnType.base[Language.Value, String](
    { enum => enum.dbName },
    { string => Language.findByString(string).getOrElse(Language.NoLanguage) }
  )
  class MovieTable(tag: Tag) extends Table[MovieExtendedForm](tag, "movies") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[String]("description")
    def releaseDate = column[LocalDate]("releaseDate")
    def country= column[Country.Value]("country")
    def language= column[Language.Value]("language")

    def * = (id, title, description,releaseDate,country,language) <> ((MovieExtendedForm.apply _).tupled, MovieExtendedForm.unapply)

  }

  val movies = TableQuery[MovieTable]
}

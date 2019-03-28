package modules

import java.time.LocalDate

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.Json
import scala.language.implicitConversions

package object movie{
  def CountryFormFormatter: Formatter[Country.Value] = new Formatter[Country.Value] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], Country.Value] = {
      val value = data.getOrElse(key, "")
      Country
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Country not found by value:'$value'"))))
    }
    def unbind(key: String, value: Country.Value): Map[String, String] = Map(key -> value.toString)
  }
  object Country extends Enumeration{
    protected case class CountryVal private[Country](dbName: String, countryName: String, nationality: String, language: String) extends super.Val

    val USA = CountryVal("US", "USA", "American", "English")
    val Australia = CountryVal("AU", "Australia", "Australian", "English")
    val Lithuania = CountryVal("LT", "Lithuania", "Lithuanian", "Lithuanian")
    val Italy = CountryVal("IT", "Italy", "Italian","Italian")
    val Spain = CountryVal("ES", "Spain", "Spanish","Spanish")
    val Sweden = CountryVal("SE", "Sweden", "Swedish","Swedish")
    val UK = CountryVal("UK", "United Kingdom", "English","English")
    val Poland = CountryVal("PL", "Poland", "Polish","Polish")


    def findByString(value: String): Option[movie.Country.Value] = {
      values.find(_.toString == value)
    }

    implicit def valueToCountryVal(x: Value): CountryVal = x.asInstanceOf[CountryVal]
}

  case class CreateMovieForm(
      title: String,
      description:String,
      releaseDate: LocalDate,
      country: String,
      language: String
  )

  case class Movie(
      id: Long,
      title: String,
      description:String,
      releaseDate: LocalDate,
      country: String,
      language: String)

  object Movie {
    implicit val movieFormat = Json.format[Movie]
  }
}

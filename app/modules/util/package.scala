package modules

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.mvc.QueryStringBindable

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

package object util {
  def CountryFormFormatter: Formatter[Country.Value] = new Formatter[Country.Value] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], Country.Value] = {
      val value = data.getOrElse(key, "")
      Country
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Country not found by value:'$value'"))))
    }
    def unbind(key: String, value: Country.Value): Map[String, String] = Map(key -> value.dbName)
  }


  object Country extends Enumeration{
    protected case class CountryVal private[Country](dbName: String, countryName: String, nationality: String, language: String) extends super.Val

    val USA = CountryVal("US", "USA", "American", "English")
    val Australia = CountryVal("AU", "Australia", "Australian", "English")
    val Lithuania = CountryVal("LT", "Lithuania", "Lithuanian", "Lithuanian")
    val Italy = CountryVal("IT", "Italy", "Italian","Italian")
    val Spain = CountryVal("ES", "Spain", "Spanish","Spanish")
    val Sweden = CountryVal("SE", "Sweden", "Swedish","Swedish")
    val UK = CountryVal("UK", "United Kingdom", "British","English")
    val Poland = CountryVal("PL", "Poland", "Polish","Polish")
    val NoCountry = CountryVal("", "", "", "")

    def findByString(value: String): Option[CountryVal] = {
      Option(
        value match {
          case "US" => USA
          case "AU" => Australia
          case "ES" => Spain
          case "SE" => Sweden
          case "LT" => Lithuania
          case "IT" => Italy
          case "UK" => UK
          case "PL" => Poland
          case _ => NoCountry
        }
      )
    }

    implicit def valueToCountryVal(x: Value): CountryVal = x.asInstanceOf[CountryVal]

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[Country.Value] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Country.Value]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(Country.findByString(s)) match {
                  case Success(country) =>
                    Right(country.get)
                  case Failure(_) =>
                    Left(s"Failed to parse country from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }
        override def unbind(key: String, country: Country.Value): String = {
          stringBinder.unbind(key, country.dbName)
        }
      }
  }
  object SortOrder extends Enumeration {
    type Order = Value
    val asc = Value("asc")
    val desc = Value("desc")

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[Order] {

        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Order]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(SortOrder.withName(s)) match {
                  case Success(sortOrder) =>
                    Right(sortOrder)
                  case Failure(_) =>
                    Left(s"Failed to parse sort order from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }

        override def unbind(key: String, sortOrder: Order): String = {
          stringBinder.unbind(key, sortOrder.toString)
        }
      }
  }
  def LanguageFormatter: Formatter[Language.Value] = new Formatter[Language.Value] {
    def bind(key: String, data: Map[String, String]): Either[List[FormError], Language.Value] = {
      val value = data.getOrElse(key, "")
      Language
        .findByString(value)
        .map(Right(_))
        .getOrElse(Left(List(FormError(key, s"Language not found by value:'$value'"))))
    }
    def unbind(key: String, value: Language.Value): Map[String, String] = Map(key -> value.dbName)
  }

  object Language extends Enumeration {
    protected case class LanguageVal private[Language](dbName: String, name: String) extends Val(dbName)

    val english = LanguageVal("EN", "English")
    val swedish = LanguageVal("SE", "Swedish")
    val spanish = LanguageVal("ES", "Spanish")
    val lithuanian = LanguageVal("LT", "Lithuanian")
    val polish = LanguageVal("PL", "Polish")
    val NoLanguage = LanguageVal("", "")

    implicit def valueToCountryVal(x: Value): LanguageVal = x.asInstanceOf[LanguageVal]

    def findByString(value: String): Option[LanguageVal] = {
      Option(
        value match {
          case "EN" => english
          case "SE" => swedish
          case "ES" => spanish
          case "LT" => lithuanian
          case "PL" => polish
          case _ => NoLanguage

        }
      )
    }

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[Language.Value] {
        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Language.Value]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(Language.findByString(s)) match {
                  case Success(language) =>
                    Right(language.get)
                  case Failure(_) =>
                    Left(s"Failed to parse language from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }
        override def unbind(key: String, language: Language.Value): String = {
          stringBinder.unbind(key, language.dbName)
        }
      }
  }
  def parseDate(date: String): Option[LocalDate] = {
    if (date == "") None
    else {
      Some(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }
  }



}

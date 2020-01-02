package modules

import java.time.LocalDate

import modules.util._
import play.api.mvc.QueryStringBindable

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

package object movie{

  case class MovieForm(
      title: String,
      description:String,
      releaseDate: LocalDate,
      country: Country.Value,
      language: Language.Value
  )

  case class MovieExtendedForm(
      id: Long,
      title: String,
      description:String,
      releaseDate: LocalDate,
      country: Country.Value,
      language: Language.Value
  )

  case class FilterMovieForm (
      title: String,
      description: String,
      releaseDate: Option[LocalDate],
      country: Country.Value,
      language: Language.Value
  )
  object SortableField extends Enumeration {
    type Field = Value
    val id = Value("id")
    val title = Value("title")
    val description = Value("description")
    val releaseDate = Value("releaseDate")
    val country = Value("country")
    val language = Value("language")

    implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) =
      new QueryStringBindable[Field] {

        override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Field]] = {
          stringBinder.bind(key, params)
            .map {
              case Right(s) =>
                Try(SortableField.withName(s)) match {
                  case Success(sortField) =>
                    Right(sortField)
                  case Failure(_) =>
                    Left(s"Failed to parse sort field from '$s'")
                }
              case Left(baseBinderFailure) =>
                Left(baseBinderFailure)
            }
        }
        override def unbind(key: String, sortField: Field): String = {
          stringBinder.unbind(key, sortField.toString)
        }
      }
  }
  case class SortItems(
      field: SortableField.Value,
      order: SortOrder.Value
  )


}

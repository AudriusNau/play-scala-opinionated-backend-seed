package modules

import java.time.LocalDate

import modules.util.{Country, Gender, SortOrder}
import play.api.mvc.QueryStringBindable

import scala.util.{Either, Failure, Left, Right, Success, Try}

package object director {


  case class DirectorForm(
      firstName: String,
      lastName:String,
      dateOfBirth: LocalDate,
      nationality: Country.Value,
      height: Int,
      gender: Gender.Value
  )

  case class DirectorExtendedForm(
      id: Long,
      firstName: String,
      lastName:String,
      dateOfBirth: LocalDate,
      nationality: Country.Value,
      height: Int,
      gender: Gender.Value
  )
  case class DirectorFilterForm(
      firstName: String,
      lastName:String,
      dateOfBirth: Option[LocalDate],
      nationality: Country.Value,
      heightMin: Int,
      heightMax: Int,
      gender: Gender.Value
  )
  object SortableField extends Enumeration {
    type Field = Value
    val id = Value("id")
    val firstName = Value("firstName")
    val lastName = Value("lastName")
    val dateOfBirth = Value("dateOfBirth")
    val nationality = Value("nationality")
    val height = Value("height")


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

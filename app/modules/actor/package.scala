package modules

import java.time.LocalDate

package object actor {


  case class CreateActorForm(
      firstName: String,
      lastName:String,
      dateOfBirth: LocalDate,
      nationality: String,
      height: Int,
      gender: String
  )

  case class Actor(
      id: Long,
      firstName: String,
      lastName:String,
      dateOfBirth: LocalDate,
      nationality: String,
      height: Int,
      gender: String
  )
}

package modules

import java.time.LocalDate

package object director {


  case class CreateDirectorForm(
      firstName: String,
      lastName:String,
      dateOfBirth: LocalDate,
      nationality: String,
      height: Int,
      gender: String
  )

  case class Director(
      id: Long,
      firstName: String,
      lastName:String,
      dateOfBirth: LocalDate,
      nationality: String,
      height: Int,
      gender: String
  )
}

package db

object Conversions {

  implicit def string2User(s: String): User = {
    val parts = s.split(":")
    User(parts(0), parts(1), "")
  }

}

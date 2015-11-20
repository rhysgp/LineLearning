package model

case class UserEmail(address: String)

case class User(id: String, email: UserEmail) {
  override def toString = s"$id:$email"
}

object User {
  def fromString(s: String) = {
    val parts = s.split(":")
    User(parts(0), UserEmail(parts(1)))
  }
}

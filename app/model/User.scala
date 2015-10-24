package model

case class User(id: String, email: String) {
  override def toString = s"$id:$email"
}

object User {
  def fromString(s: String) = {
    val parts = s.split(":")
    User(parts(0), parts(1))
  }
}

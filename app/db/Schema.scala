package db


import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global

class Schema {

  class User(tag: Tag) extends Table[(String, String, String)](tag, "user") {
    def id = column[String]("id", O.PrimaryKey)
    def email = column[String]("email")
    def password = column[String]("password")

    def * = (id, email, password)
  }

  val users = TableQuery[User]


  class Scene(tag: Tag) extends Table[(String, String, String)](tag, "scene") {
    def id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")
    def userId = column[String]("user_id")

    // foreign key to user table:
    def user = foreignKey("user_fk", userId, users)(_.id)

    def * = (id, name, userId)
  }

  class CueLine(tag: Tag) extends Table[(String, String, String)](tag, "cue_line") {
    def id = column[String]("id", O.PrimaryKey)
    def cue = column[String]("cue")
    def line = column[String]("line")

    def * = (id, cue, line)
  }

}





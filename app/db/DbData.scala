package db


import java.util.UUID

import model.{UserEmail, User}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import DbData._

object DbData {
  val users = TableQuery[Users]
  val scenes = TableQuery[Scenes]
  val cueLines = TableQuery[CueLines]

  val findUserByEmail = DbData.users.findBy(_.email)
  def createUser(emailAddress: String) = {
    val guid = UUID.randomUUID().toString
    DbData.users += (guid, emailAddress, "password")
    User(guid, UserEmail(emailAddress))
  }

  def

  def schemaCreate = (users.schema ++ scenes.schema ++ cueLines.schema).create


}

class Users(tag: Tag) extends Table[(String, String, String)](tag, "user") {
  def id = column[String]("id", O.PrimaryKey)
  def email = column[String]("email")
  def password = column[String]("password")

  def * = (id, email, password)
}


class Scenes(tag: Tag) extends Table[(String, String, String)](tag, "scene") {
  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name")
  def userId = column[String]("user_id")

  // foreign key to user table:
  def user = foreignKey("user_fk", userId, users)(_.id)

  def * = (id, name, userId)
}

class CueLines(tag: Tag) extends Table[(String, String, String)](tag, "cue_line") {
  def id = column[String]("id", O.PrimaryKey)
  def cue = column[String]("cue")
  def line = column[String]("line")

  def * = (id, cue, line)
}






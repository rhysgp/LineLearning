package db


import java.util.UUID

//import model.{UserEmail, User}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import DbData._



object DbData {
  val users = TableQuery[Users]
  val scenes = TableQuery[Scenes]
  val cueLines = TableQuery[CueLines]

  val findUserByEmail = DbData.users.findBy(_.email)
  val findSceneById = DbData.scenes.findBy(_.id)
  val findUserScenes = DbData.scenes.findBy(_.userId)

  def createUser(emailAddress: String) = {
    val guid = UUID.randomUUID().toString
    DbData.users += db.User(guid, emailAddress, "password")
    User(guid, emailAddress, "")
  }

  def schemaCreate = (users.schema ++ scenes.schema ++ cueLines.schema).create

  def addScene(user: User, sceneName: String) = {

  }
}

case class User(id: String, email: String, password: String) {
  override def toString = s"$id:$email"
}

class Users(tag: Tag) extends Table[User](tag, "user") {
  def id = column[String]("id", O.PrimaryKey)
  def email = column[String]("email")
  def password = column[String]("password")

  def * = (id, email, password) <> (db.User.tupled, db.User.unapply)
}

case class Scene(id: String, name: String, userId: String)

class Scenes(tag: Tag) extends Table[Scene](tag, "scene") {
  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name")
  def userId = column[String]("user_id")

  // foreign key to user table:
  def user = foreignKey("user_fk", userId, users)(_.id)

  def * = (id, name, userId) <> (db.Scene.tupled, db.Scene.unapply)
}

case class CueLine(id: String, cue: String, line: String)
//object CueLine {
//  def apply(cue: String, line: String): CueLine = CueLine(UUID.randomUUID().toString, cue, line)
//}

class CueLines(tag: Tag) extends Table[CueLine](tag, "cue_line") {
  def id = column[String]("id", O.PrimaryKey)
  def cue = column[String]("cue")
  def line = column[String]("line")

  def * = (id, cue, line) <> (db.CueLine.tupled, db.CueLine.unapply)
}






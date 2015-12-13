package db


import java.util.UUID

import db.DbData._
import slick.driver.H2Driver.api._



object DbData {
  val users = TableQuery[Users]
  val scenes = TableQuery[Scenes]
  val cueLines = TableQuery[CueLines]

  def userExists(userId: String) =  users.filter(_.id === userId).exists

//  def userWithEmailExists(email: String) = users.filter(_.email == email).exists

  val findUserById = DbData.users.findBy(_.id)
  val findUserByEmail = DbData.users.findBy(_.email)
  val findSceneById = DbData.scenes.findBy(_.id)
  val findUserScenes = DbData.scenes.findBy(_.userId)

  def createUser(user: db.User) = {
    DbData.users += user
  }

  def schemaCreate = (users.schema ++ scenes.schema ++ cueLines.schema).create

}

case class User(id: String, email: String, password: String) {
  override def toString = s"$id:$email"
}

class Users(tag: Tag) extends Table[User](tag, "user") {
  def id = column[String]("id", O.PrimaryKey)
  def email = column[String]("email")
  def password = column[String]("password")
  def emailUniqueIdx = index("email_unique_index", email, unique = true)

  def * = (id, email, password) <> (db.User.tupled, db.User.unapply)
}

case class Scene(id: String, name: String, userId: String)

class Scenes(tag: Tag) extends Table[Scene](tag, "scene") {
  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name")
  def userId = column[String]("user_id")
  def userFk = foreignKey("user_fk", userId, users)(_.id)
  def nameUserIdIdx = index("name_user_index", (name, userId), unique = true)

  def * = (id, name, userId) <> (db.Scene.tupled, db.Scene.unapply)
}

case class CueLine(id: String, cue: String, line: String, order: Int)

class CueLines(tag: Tag) extends Table[CueLine](tag, "cue_line") {
  def id = column[String]("id", O.PrimaryKey)
  def cue = column[String]("cue")
  def line = column[String]("line")
  def order = column[Int]("order")
  def sceneId = column[String]("scene_id")
  def sceneFk = foreignKey("scene_fk", sceneId, scenes)(_.id)
  def sceneOrderIdx = index("scene_id_order_index", (sceneId, order), unique = true)

  def * = (id, cue, line, order) <> (db.CueLine.tupled, db.CueLine.unapply)
}






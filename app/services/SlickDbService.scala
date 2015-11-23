package services

import model._
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.Try

import db._

class SlickDbService(dbConfig: DatabaseConfig[JdbcProfile]) extends DbService {
  import dbConfig.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def scene(sceneId: SceneId): Try[Scene] = ???

  override def removeCueLine(sceneId: SceneId, cueLineId: CueLineId): Try[Seq[CueLine]] = ???

  override def removeScene(sceneId: SceneId): Try[Scene] = ???

  override def addCueLine(sceneId: SceneId, cl: CueLine): Try[Seq[CueLine]] = ???

  override def setCueLines(sceneId: SceneId, newLines: Lines): Try[Seq[CueLine]] = ???

  override def saveCueLine(cl: CueLine): Try[Seq[CueLine]] = ???

  override def loadCueLines(sceneId: SceneId): Try[Seq[CueLine]] = ???

  override def loadScenes(user: User): Try[Seq[Scene]] = ???

  override def renameScene(sceneId: SceneId, newName: String): Try[Scene] = ???

  override def addScene(user: User, sceneName: String): Try[Seq[Scene]] = ???


  override def addOrFindUser(email: String): Try[User] = ???

  def createDb(): Unit = {
    dbConfig.db.run(DBIO.seq(DbData.schemaCreate))
  }

  def addScene1(user: User, sceneName: String): Future[Seq[Scene]] = {

  }

  def addOrFindUser1(email: String): Future[User] = {
    dbConfig.db.run(DbData.findUserByEmail(email).result.headOption)
      .map{
        case Some((id, emailAddress, _)) => User(id, UserEmail(emailAddress))
        case _ => DbData.createUser(email)
      }
  }
}

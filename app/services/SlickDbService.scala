package services

import java.util.UUID

import model._
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.Try

import db._


trait DbServiceAsync {

  def scene(sceneId: SceneId): Future[Scene]
  def loadScenes(user: User): Future[Seq[Scene]]
  def addScene(user: User, sceneName: String): Future[Seq[Scene]]
  def removeScene(sceneId: SceneId): Future[Scene]
  def renameScene(sceneId: SceneId, newName: String): Future[Scene]

  def loadCueLines(sceneId: SceneId): Future[Seq[CueLine]]
  def addCueLine(sceneId: SceneId, cl: CueLine): Future[Seq[CueLine]]
  def removeCueLine(sceneId: SceneId, cueLineId: CueLineId): Future[Seq[CueLine]]
  def saveCueLine(cl: CueLine): Future[Seq[CueLine]]

  def setCueLines(sceneId: SceneId, newLines: Lines): Future[Seq[CueLine]]

  def addOrFindUser(email: String): Future[User]
}


class SlickDbService(dbConfig: DatabaseConfig[JdbcProfile]) extends DbServiceAsync {
  import dbConfig.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def scene(sceneId: SceneId): Future[Scene] = ???

  override def removeCueLine(sceneId: SceneId, cueLineId: CueLineId): Future[Seq[CueLine]] = ???

  override def removeScene(sceneId: SceneId): Future[Scene] = ???

  override def addCueLine(sceneId: SceneId, cl: CueLine): Future[Seq[CueLine]] = ???

  override def setCueLines(sceneId: SceneId, newLines: Lines): Future[Seq[CueLine]] = ???

  override def saveCueLine(cl: CueLine): Future[Seq[CueLine]] = ???

  override def loadCueLines(sceneId: SceneId): Future[Seq[CueLine]] = ???

  override def loadScenes(user: User): Future[Seq[Scene]] = ???

  override def renameScene(sceneId: SceneId, newName: String): Future[Scene] = ???

  override def addScene(user: User, sceneName: String): Future[Seq[Scene]] = {
    val sceneId = UUID.randomUUID().toString
    dbConfig.db.run(DBIO.seq(
      DbData.scenes += (sceneId, sceneName, user.id)
    )).flatMap( u =>
      dbConfig.db.run(DbData.findUserScenes(user.id).result).map{ tupleSeq =>
        tupleSeq.map{
          case (a, b, c) => Scene(SceneId(a), b)
        }
      }
    )
  }


  override def addOrFindUser(email: String): Future[User] = {
    dbConfig.db.run(DbData.findUserByEmail(email).result.headOption)
      .map{
        case Some((id, emailAddress, _)) => User(id, UserEmail(emailAddress))
        case _ => DbData.createUser(email)
      }
  }

  def createDb(): Unit = {
    dbConfig.db.run(DBIO.seq(DbData.schemaCreate))
  }

}

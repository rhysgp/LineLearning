package services

import java.util.UUID
import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.Lines
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.Try

import db._

@ImplementedBy(classOf[SlickDbService])
trait DbServiceAsync {

  def scene(sceneId: String): Future[Scene]
  def loadScenes(user: User): Future[Seq[Scene]]
  def addScene(user: User, sceneName: String): Future[Seq[Scene]]
  def removeScene(sceneId: String): Future[Scene]
  def renameScene(sceneId: String, newName: String): Future[Scene]

  def loadCueLines(sceneId: String): Future[Seq[CueLine]]
  def addCueLine(sceneId: String, cl: CueLine): Future[Seq[CueLine]]
  def removeCueLine(sceneId: String, cueLineId: String): Future[Seq[CueLine]]
  def saveCueLine(cl: CueLine): Future[Seq[CueLine]]

  def setCueLines(sceneId: String, newLines: Lines): Future[Seq[CueLine]]

  def addOrFindUser(email: String): Future[User]
}


class SlickDbService @Inject() (dbConfigProvider: DatabaseConfigProvider) extends DbServiceAsync {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def scene(sceneId: String): Future[Scene] = ???

  override def removeCueLine(sceneId: String, cueLineId: String): Future[Seq[CueLine]] = ???

  override def removeScene(sceneId: String): Future[Scene] = ???

  override def addCueLine(sceneId: String, cl: CueLine): Future[Seq[CueLine]] = ???

  override def setCueLines(sceneId: String, newLines: Lines): Future[Seq[CueLine]] = ???

  override def saveCueLine(cl: CueLine): Future[Seq[CueLine]] = ???

  override def loadCueLines(sceneId: String): Future[Seq[CueLine]] = ???

  override def loadScenes(user: User): Future[Seq[Scene]] = ???

  override def renameScene(sceneId: String, newName: String): Future[Scene] = ???

  override def addScene(user: User, sceneName: String): Future[Seq[db.Scene]] = {
    val sceneId = UUID.randomUUID().toString
    dbConfig.db.run(DBIO.seq(
      DbData.scenes += db.Scene(sceneId, sceneName, user.id)
    )).flatMap( u =>
      dbConfig.db.run(DbData.findUserScenes(user.id).result)
    )
  }


  override def addOrFindUser(email: String): Future[db.User] = {
    dbConfig.db.run(DbData.findUserByEmail(email).result.headOption)
      .map(_.getOrElse(DbData.createUser(email)))
  }

  def createDb(): Unit = {
    dbConfig.db.run(DBIO.seq(DbData.schemaCreate))
  }

}

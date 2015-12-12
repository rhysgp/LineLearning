package services

import java.util.UUID
import javax.inject.{Singleton, Inject}

import com.google.inject.ImplementedBy
import db._
import model.Lines
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.Future

@ImplementedBy(classOf[SlickDbService])
trait DbServiceAsync {

  def scene(sceneId: String): Future[Scene]
  def loadScenes(user: User): Future[Seq[Scene]]
  def addScene(user: User, sceneName: String): Future[Unit]
  def removeScene(user: User, sceneId: String): Future[Scene]
  def renameScene(sceneId: String, newName: String): Future[Scene]

  def loadCueLines(sceneId: String): Future[Seq[CueLine]]
  def addCueLine(sceneId: String, cl: CueLine): Future[Seq[CueLine]]
  def removeCueLine(sceneId: String, cueLineId: String): Future[Seq[CueLine]]
  def saveCueLine(cl: CueLine): Future[Seq[CueLine]]

  def setCueLines(sceneId: String, newLines: Lines): Future[Seq[CueLine]]

  def addOrFindUser(email: String): Future[User]
}

class SceneNotFoundException(val sceneName: Scene) extends Exception
class AlreadyExistsException(msg: String) extends Exception(msg)
class NoSuchSceneException(sceneId: String) extends Exception(s"Scene '$sceneId' doesn't exist.")
class NoSuchUserException(email: String) extends Exception(s"$email is not registered.")
class NoSuchCueLineException extends Exception


@Singleton
class SlickDbService @Inject() (dbConfigProvider: DatabaseConfigProvider) extends DbServiceAsync {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  @volatile var created = false

  override def scene(sceneId: String): Future[Scene] = ???

  override def removeCueLine(sceneId: String, cueLineId: String): Future[Seq[CueLine]] = ???

  override def removeScene(user: User, sceneId: String): Future[Scene] = ???

  override def addCueLine(sceneId: String, cl: CueLine): Future[Seq[CueLine]] = ???

  override def setCueLines(sceneId: String, newLines: Lines): Future[Seq[CueLine]] = ???

  override def saveCueLine(cl: CueLine): Future[Seq[CueLine]] = ???

  override def loadCueLines(sceneId: String): Future[Seq[CueLine]] = ???

  override def loadScenes(user: User): Future[Seq[Scene]] = {


    dbConfig.db.run(DbData.users.result).map(results => {
      println("---- users ----")
      results.map(user => s"${user.id} --> ${user.email}")
      println("---------------")
    })


    dbConfig.db.run(DbData.userExists(user.id).result)
      .flatMap{ exists =>
        if (!exists) throw new NoSuchUserException(user.email)
        dbConfig.db.run(DbData.findUserScenes(user.id).result)
      }
  }

  override def renameScene(sceneId: String, newName: String): Future[Scene] = ???

  override def addScene(user: User, sceneName: String): Future[Unit] = {

    val sceneId = UUID.randomUUID().toString

    println(s"====---> Inserting ($sceneId, $sceneName, ${user.id})")

    dbConfig.db.run(
      DbData.scenes += db.Scene(sceneId, sceneName, user.id)
    ).map(_ => Unit)
  }

  override def addOrFindUser(email: String): Future[db.User] = {

    if (!created) {
      createDb()
      created = true
    }

    dbConfig.db.run(
      DbData.findUserByEmail(email).result
        .map(q => DbData.createUser(email))
    )

    dbConfig.db.run(
      DbData.createUser(email)
        .andThen(DbData.findUserByEmail(email).result.head)
    )

//    Future(User("", "", ""))
  }

  def createDb(): Unit = {
    println("***** createDb() *****")
    dbConfig.db.run(DBIO.seq(DbData.schemaCreate))
  }

}

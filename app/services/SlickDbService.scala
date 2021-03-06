package services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import db._
import model.Lines
import org.mindrot.jbcrypt.BCrypt
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.Future

@ImplementedBy(classOf[SlickDbService])
trait DbServiceAsync {

  def scene(sceneId: String): Future[Scene]
  def loadScenes(user: User): Future[Seq[Scene]]
  def addScene(user: User, sceneName: String): Future[Unit]
  def removeScene(user: User, sceneId: String): Future[Boolean]
  def renameScene(sceneId: String, newName: String): Future[Scene]

  def loadCueLines(sceneId: String): Future[Seq[CueLine]]
  def addCueLine(sceneId: String, cl: CueLine): Future[Boolean]
  def removeCueLine(sceneId: String, cueLineId: String): Future[Boolean]
  def saveCueLine(cl: CueLine): Future[Int]

  def setCueLines(sceneId: String, newLines: Lines): Future[Seq[CueLine]]

  def createUser(email: String): Future[User]

  def findUser(email: String, password: String): Future[User]

  def changePassword(email: String, oldPassword: String, newPassword: String): Future[Unit]
  def resetPassword(email: String): Future[String]
}

class SceneNotFoundException(val sceneName: Scene) extends Exception
class AlreadyExistsException(msg: String) extends Exception(msg)
class NoSuchSceneException(sceneId: String) extends Exception(s"Scene '$sceneId' doesn't exist.")
class NoSuchUserException(email: String) extends Exception(s"$email is not registered.")
class NoSuchCueLineException extends Exception
class PasswordChangeException extends Exception


@Singleton
class SlickDbService @Inject() (dbConfigProvider: DatabaseConfigProvider, configuration: Configuration) extends DbServiceAsync {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val shouldCreateDb = configuration.getBoolean("slickdb.create").contains(true)

  import dbConfig.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  createDb()

  override def scene(sceneId: String): Future[Scene] = {
    dbConfig.db.run(DbData.findSceneById(sceneId).result.head)
  }

  override def removeCueLine(sceneId: String, cueLineId: String): Future[Boolean] = {
    dbConfig.db.run(DbData.cueLines.filter(cl => cl.id === cueLineId).delete)
      .map(delCount => {
        if (delCount > 1) Logger.error(s"Deleted $delCount cue lines from scene $sceneId")
        delCount > 0
      })
  }

  override def removeScene(user: User, sceneId: String): Future[Boolean] = {
//    dbConfig.db.run(
//      DbData.cueLines.filter(cl => cl.sceneId === sceneId).delete
//        .map(x => DbData.scenes.filter(s => s.id === sceneId && s.userId === user.id).delete)
//    )
//    .map(x => true)

    dbConfig.db.run(DBIO.seq(
      DbData.cueLines.filter(cl => cl.sceneId === sceneId).delete,
      DbData.scenes.filter(s => s.id === sceneId && s.userId === user.id).delete
    ))

    Future(true)

      // FIXME - need to figure out how to get the count out of the final delete

//      .map(delCount => {
//        if (delCount > 1) Logger.error(s"Deleted $delCount scenes! - expecting only one!")
//        delCount > 0
//      })
  }

  override def addCueLine(sceneId: String, cl: CueLine): Future[Boolean] = {
    dbConfig.db.run(DbData.cueLines.filter(cl => cl.sceneId === sceneId).result)
      .flatMap(count => {
        val clWithCount = cl.copy(order = count.length)
        dbConfig.db.run(DbData.cueLines += clWithCount)
      })
      .map(x => true)
  }

  override def setCueLines(sceneId: String, newLines: Lines): Future[Seq[CueLine]] = ???

  override def saveCueLine(cl: CueLine): Future[Int] = {
    dbConfig.db.run(DbData.cueLines.filter(_.id === cl.id).map(row => (row.cue, row.line)).update((cl.cue, cl.line)))
  }

  override def loadCueLines(sceneId: String): Future[Seq[CueLine]] = {
    dbConfig.db.run(DbData.cueLines.filter(_.sceneId === sceneId).sortBy(_.order.asc).result)
  }

  override def loadScenes(user: User): Future[Seq[Scene]] = {
    dbConfig.db.run(DbData.userExists(user.id).result)
      .flatMap{ exists =>
        if (!exists) throw new NoSuchUserException(user.email)
        dbConfig.db.run(DbData.findUserScenes(user.id).sortBy(_.name.desc).result)
      }
  }

  override def renameScene(sceneId: String, newName: String): Future[Scene] = {
    dbConfig.db.run(DbData.scenes.filter(s => s.id === sceneId).map(_.name).update(newName))
      .flatMap(updCount => {
        if (updCount != 1) Logger.error(s"renameScene(): Expecting update count to be 1, but was $updCount")
        dbConfig.db.run(DbData.scenes.filter(s => s.id === sceneId).result.head)
      })
  }

  override def addScene(user: User, sceneName: String): Future[Unit] = {

    val sceneId = UUID.randomUUID().toString

    dbConfig.db.run(
      DbData.scenes += db.Scene(sceneId, sceneName, user.id)
    ).map(_ => Unit)
  }

  override def createUser(email: String): Future[db.User] = {
    val password = UUID.randomUUID().toString
    val encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
    val user = User(UUID.randomUUID().toString, email, encryptedPassword)
    dbConfig.db.run(DbData.createUser(user))
      .map(x => user.copy(password = password))
  }

  def findUser(email: String, password: String): Future[User] = {
    dbConfig.db.run(DbData.users.filter(user => user.email === email).result)
      .map(_.head)
      .filter(user =>
        BCrypt.checkpw(password, user.password)
      )
  }

  def changePassword(email: String, oldPassword: String, newPassword: String): Future[Unit] = {
    val newEncryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
    dbConfig.db.run(DbData.users.filter(user => user.email === email).result)
      .map(_.head)
      .filter(user => BCrypt.checkpw(oldPassword, user.password))
      .flatMap(user => {
        dbConfig.db.run(
          DbData.users.filter(user => user.email === email).map(_.password).update(newEncryptedPassword)
        ).map(updateCount => {
          if (updateCount == 0) throw new PasswordChangeException()
        })
      })
      .recover{
        case _ => throw new PasswordChangeException()
      }
  }

  def resetPassword(email: String): Future[String] = {
    val newPassword = UUID.randomUUID().toString
    val encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
    dbConfig.db.run(
      DbData.users.filter(user => user.email === email).map(_.password).update(encryptedPassword)
    ).map(updateCount => {
      if (updateCount == 1) {
        newPassword
      } else {
        throw new NoSuchUserException(email)
      }
    })
  }

  def createDb(): Unit = {
    if (shouldCreateDb) {
      dbConfig.db.run(DBIO.seq(DbData.schemaCreate))
    }
  }
}

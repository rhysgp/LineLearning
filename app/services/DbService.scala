package services

import java.util.UUID

import model.{Scene, SceneName, Lines, CueLine}

import scala.util.{Success, Try}

trait DbService {

  def loadScenes(user: User): Seq[SceneName]
  def addScene(user: User, sceneName: String): Try[SceneName]
  def removeScene(scene: SceneName): Try[SceneName]
  def renameScene(scene: SceneName, newName: String): Try[SceneName]

  def loadCueLines(scene: SceneName): Seq[CueLine]
  def addCueLine(scene: SceneName, cl: CueLine): Seq[CueLine]
  def removeCueLine(scene: SceneName, cl: CueLine): Try[CueLine]
  def saveCueLine(cl: CueLine): Unit

  def setCueLines(scene: SceneName, lines: Lines): Unit

  def addOrFindUser(email: String): User
}

class SceneNotFoundException(val sceneName: SceneName) extends Exception

object InMemoryDbService extends DbService {

  var scenes = Seq[SceneName]()
  var lines = Seq[(SceneName, CueLine)]()
  var users = Seq[User]()

  def loadScenes(user: User): Seq[SceneName] = {
    scenes.filter(_.user == user)
  }

  def addScene(user: User, sceneName: String): SceneName = {
    val sn = SceneName(user, sceneName)
    scenes = scenes :+ sn
    sn
  }

  def removeScene(scene: SceneName): Try[SceneName] = Try {
    if (scenes.contains(scene)) {
      scenes.find(_ == scene).get
    } else {
      throw new SceneNotFoundException(scene)
    }
  }

  override def renameScene(scene: SceneName, newName: String): Try[SceneName] = Try {
    if (scenes.contains(scene)) {
      val newScene = scene.copy(name = newName)
      scenes = scenes.filterNot(_ == scene) :+ newScene
      newScene
    } else {
      throw new SceneNotFoundException(scene)
    }
  }

  def loadCueLines(scene: SceneName): Seq[CueLine] = {
    lines.filter{case (sn, cl) => sn == scene}
  }

  def addCueLine(userId: User, cl: CueLine) = {
    val currentUserAssignments: Seq[String] = userLines.getOrElse(userId, Seq())

    if (!lines.contains(cl.id)) {
      lines = lines + (cl.id -> cl)
    }

    userLines = userLines + (userId -> (currentUserAssignments :+ cl.id))
    userLines(userId).map(lines)
  }

  def saveCueLine(cl: CueLine): Unit = {
    lines = lines.filterNot(_._1 == cl.id) + (cl.id -> cl)
  }

  def setCueLines(userId: User, newLines: Lines): Unit = {
    lines = newLines.lines.map(cl => cl.id -> cl).foldLeft(lines)((map, entry) => {
      map + entry
    })
  }
  
  def addOrFindUser(emailAddress: String): User = {
    users
      .find(_.email == emailAddress)
      .getOrElse {
        val user = User(UUID.randomUUID().toString, emailAddress)
        users = users :+ user
        user
      }
  }
}

object DbService extends DbService {



//  def loadCueLines(userId: User) = InMemoryDbService.loadCueLines(userId)
//  def addCueLine(userId: User, cl: CueLine) = InMemoryDbService.addCueLine(userId, cl)
//  def saveCueLine(cl: CueLine) = InMemoryDbService.saveCueLine(cl)
//  def setCueLines(userId: User, lines: Lines) = InMemoryDbService.setCueLines(userId, lines)
//
//  def addOrFindUser(email: String) = InMemoryDbService.addOrFindUser(email)
  override def loadScenes(user: User): Seq[Scene] = ???

  override def removeCueLine(user: User, scene: SceneName, cl: CueLine): Try[CueLine] = ???

  override def removeScene(user: User, scene: SceneName): Try[SceneName] = ???

  override def addCueLine(user: User, scene: SceneName, cl: CueLine): Seq[CueLine] = ???

  override def setCueLines(scene: SceneName, lines: Lines): Unit = ???

  override def saveCueLine(cl: CueLine): Unit = ???

  override def loadCueLines(user: User, scene: SceneName): Seq[CueLine] = ???

  override def addScene(user: User, sceneName: String): Try[SceneName] = ???

  override def renameScene(user: User, scene: SceneName, newName: String): Try[SceneName] = ???

  override def addOrFindUser(email: String): User = ???
}


case class User(id: String, email: String) {
  override def toString = s"$id:$email"
}

object User {
  def fromString(s: String) = {
    val parts = s.split(":")
    User(parts(0), parts(1))
  }
}

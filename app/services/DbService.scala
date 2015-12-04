package services

import db._
import model.Lines

import scala.util.Try

trait DbService {

  def scene(sceneId: String): Try[Scene]
  def loadScenes(user: User): Try[Seq[Scene]]
  def addScene(user: User, sceneName: String): Try[Seq[Scene]]
  def removeScene(sceneId: String): Try[Scene]
  def renameScene(sceneId: String, newName: String): Try[Scene]

  def loadCueLines(sceneId: String): Try[Seq[CueLine]]
  def addCueLine(sceneId: String, cl: CueLine): Try[Seq[CueLine]]
  def removeCueLine(sceneId: String, cueLineId: String): Try[Seq[CueLine]]
  def saveCueLine(cl: CueLine): Try[Seq[CueLine]]

  def setCueLines(sceneId: String, newLines: Lines): Try[Seq[CueLine]]

  def addOrFindUser(email: String): Try[User]
}

class SceneNotFoundException(val sceneName: Scene) extends Exception
class AlreadyExistsException(msg: String) extends Exception(msg)
class NoSuchSceneException(sceneId: String) extends Exception(s"Scene '$sceneId' doesn't exist.")
class NoSuchUserException(email: String) extends Exception(s"$email is not registered.")
class NoSuchCueLineException extends Exception

//object InMemoryDbService extends DbService {
//
//  var scenes = Seq[Scene]()
//  var lines = Seq[(String, CueLine)]()
//  var users = Seq[User]()
//  var userScenes = Seq[(String, String)]()
//
//  private def userSceneIds(user: User): Seq[String] =
//    userScenes.filter{ case (_, email) => email == user.email }.map{ case (sceneId, _) => sceneId }
//
//  private def scenesForUser(user: User): Seq[Scene] = {
//    val sceneIds =  userSceneIds(user)
//    scenes.filter(scene => sceneIds.contains(scene.id))
//  }
//
//  private def checkExistentialAngst(user: User) =
//    if (!users.contains(user)) { throw new NoSuchUserException(user.email) }
//
//  private def findScene(sceneId: String): Scene =
//    scenes.find(_.id == sceneId).getOrElse(throw new NoSuchSceneException(sceneId))
//
//  def scene(sceneId: String) = Try { findScene(sceneId) }
//
//  def loadScenes(user: User): Try[Seq[Scene]] = Try { loadScenesImpl(user) }
//
//  def loadScenesImpl(user: User): Seq[Scene] = {
//    checkExistentialAngst(user)
//    scenesForUser(user)
//  }
//
//  def addScene(user: User, sceneName: String): Try[Seq[Scene]] = Try {
//
//    checkExistentialAngst(user)
//
//    if (scenesForUser(user).exists(s => s.name == sceneName))
//      throw new AlreadyExistsException(s"Scene '$sceneName' already exists.")
//
//    val newScene = Scene(String.create(), sceneName)
//    scenes = scenes :+ newScene
//    userScenes = userScenes :+ (newScene.id, user.email)
//
//    loadScenesImpl(user)
//  }
//
//  def removeScene(sceneId: String): Try[Scene] = Try {
//    val scene = findScene(sceneId)
//    lines = lines.filter(_._1 == sceneId)
//    scenes = scenes.filterNot(_ == scene)
//    scene
//  }
//
//  override def renameScene(sceneId: String, newName: String): Try[Scene] = Try {
//    val scene = findScene(sceneId)
//    if (scenes.contains(scene)) {
//      val newScene = scene.copy(name = newName)
//      scenes = scenes.filterNot(_ == scene) :+ newScene
//      newScene
//    } else {
//      throw new SceneNotFoundException(scene)
//    }
//  }
//
//  def loadCueLinesImpl(sceneId: String): Seq[CueLine] = {
//
//    findScene(sceneId) // check it exists
//
//    lines
//      .filter{case (sId, _) => sId == sceneId}
//      .map{case (_, cl) => cl}
//  }
//
//  def loadCueLines(sceneId: String): Try[Seq[CueLine]] = Try{
//    loadCueLinesImpl(sceneId)
//  }
//
//  def addCueLine(sceneId: String, cueLine: CueLine): Try[Seq[CueLine]] = Try{
//
//    val scene = findScene(sceneId)
//
//    if (!lines.exists{case (_, cl) => cl.cueLineId == cueLine.cueLineId}) {
//      lines = lines :+ (scene.id -> cueLine)
//    } else {
//      saveCueLine(cueLine)
//    }
//
//    loadCueLinesImpl(sceneId)
//  }
//
//  override def removeCueLine(sceneId: String, cueLineId: CueLineId): Try[Seq[CueLine]] = Try {
//    lines = lines.filterNot{case (s, cl) => cl.cueLineId == cueLineId}
//    loadCueLinesImpl(sceneId)
//  }
//
//  def saveCueLine(cueLine: CueLine): Try[Seq[CueLine]] = Try{
//    val linesEntryOpt = lines.find{case(_, cl) => cl.cueLineId == cl.cueLineId}
//    linesEntryOpt match {
//      case Some((s, cl)) =>
//        lines = lines.map(entry => {if (entry._2.cueLineId == cueLine.cueLineId) (entry._1, cueLine) else entry})
//        loadCueLinesImpl(s)
//      case None =>
//        throw new NoSuchCueLineException
//    }
//  }
//
//  def setCueLines(sceneId: String, newLines: Lines): Try[Seq[CueLine]] = Try {
//    lines.filter{case (s, _) => s == sceneId} ++ newLines.lines.map(cl => sceneId -> cl)
//    loadCueLinesImpl(sceneId)
//  }
//
//  def addOrFindUser(emailAddress: String): Try[User] = Try{
//    users
//      .find(_.email.address == emailAddress)
//      .getOrElse {
//        val user = User(UUID.randomUUID().toString, UserEmail(emailAddress))
//        users = users :+ user
//        user
//      }
//  }
//}

//object DbService extends DbService {
//  override def scene(sceneId: String): Try[Scene] = InMemoryDbService.scene(sceneId)
//  override def loadScenes(user: User): Try[Seq[Scene]] = InMemoryDbService.loadScenes(user)
//  override def removeCueLine(sceneId: String, clId: CueLineId): Try[Seq[CueLine]] = InMemoryDbService.removeCueLine(sceneId, clId)
//  override def removeScene(sceneId: String): Try[Scene] = InMemoryDbService.removeScene(sceneId)
//  override def addCueLine(sceneId: String, cl: CueLine): Try[Seq[CueLine]] = InMemoryDbService.addCueLine(sceneId, cl)
//  override def setCueLines(sceneId: String, newLines: Lines): Try[Seq[CueLine]] = InMemoryDbService.setCueLines(sceneId, newLines)
//  override def saveCueLine(cl: CueLine): Try[Seq[CueLine]] = InMemoryDbService.saveCueLine(cl)
//  override def loadCueLines(sceneId: String): Try[Seq[CueLine]] = InMemoryDbService.loadCueLines(sceneId)
//  override def addScene(user: User, sceneName: String): Try[Seq[Scene]] = InMemoryDbService.addScene(user, sceneName)
//  override def renameScene(sceneId: String, newName: String): Try[Scene] = InMemoryDbService.renameScene(sceneId, newName)
//  override def addOrFindUser(email: String): Try[User] = InMemoryDbService.addOrFindUser(email)
//}


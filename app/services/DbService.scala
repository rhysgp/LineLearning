package services

import java.util.UUID

import model._

import scala.util.Try

trait DbService {

  def loadScenes(user: User): Try[Seq[Scene]]
  def addScene(user: User, sceneName: String): Try[Seq[Scene]]
  def removeScene(scene: Scene): Try[Scene]
  def renameScene(scene: Scene, newName: String): Try[Scene]

  def loadCueLines(sceneId: SceneId): Try[Seq[CueLine]]
  def addCueLine(sceneId: SceneId, cl: CueLine): Try[Seq[CueLine]]
  def removeCueLine(sceneId: SceneId, cueLineId: CueLineId): Try[Seq[CueLine]]
  def saveCueLine(cl: CueLine): Try[Seq[CueLine]]

  def setCueLines(sceneId: SceneId, newLines: Lines): Try[Seq[CueLine]]

  def addOrFindUser(email: String): Try[User]
}

class SceneNotFoundException(val sceneName: Scene) extends Exception

object InMemoryDbService extends DbService {

  var scenes = Seq[Scene]()
  var lines = Seq[(SceneId, CueLine)]()
  var users = Seq[User]()
  var userScenes = Seq[(SceneId, UserEmail)]()

  private def userSceneIds(user: User): Seq[SceneId] =
    userScenes.filter{ case (_, email) => email == user.email }.map{ case (sceneId, _) => sceneId }

  private def scenesForUser(user: User): Seq[Scene] = {
    val sceneIds =  userSceneIds(user)
    scenes.filter(scene => sceneIds.contains(scene.id))
  }

  private def checkExistentialAngst(user: User) =
    if (!users.contains(user)) { throw new NoSuchUserException(user.email.address) }

  private def findScene(sceneId: SceneId): Scene =
    scenes.find(_.id == sceneId).getOrElse(throw new NoSuchSceneException(sceneId))

  def loadScenes(user: User): Try[Seq[Scene]] = Try { loadScenesImpl(user) }

  def loadScenesImpl(user: User): Seq[Scene] = {
    checkExistentialAngst(user)
    scenesForUser(user)
  }

  def addScene(user: User, sceneName: String): Try[Seq[Scene]] = Try {

    checkExistentialAngst(user)

    if (scenesForUser(user).exists(s => s.name == sceneName))
      throw new AlreadyExistsException(s"Scene '$sceneName' already exists.")

    val newScene = Scene(SceneId.create(), sceneName)
    scenes = scenes :+ newScene
    userScenes = userScenes :+ (newScene.id, user.email)

    loadScenesImpl(user)
  }

  def removeScene(scene: Scene): Try[Scene] = Try {
    if (scenes.contains(scene)) {
      lines = lines.filter(_._1 == scene)
      scenes = scenes.filterNot(_ == scene)
      scene
    } else {
      throw new SceneNotFoundException(scene)
    }
  }

  override def renameScene(scene: Scene, newName: String): Try[Scene] = Try {
    if (scenes.contains(scene)) {
      val newScene = scene.copy(name = newName)
      scenes = scenes.filterNot(_ == scene) :+ newScene
      newScene
    } else {
      throw new SceneNotFoundException(scene)
    }
  }

  def loadCueLinesImpl(sceneId: SceneId): Seq[CueLine] = {

    findScene(sceneId) // check it exists

    lines
      .filter{case (sId, _) => sId == sceneId}
      .map{case (_, cl) => cl}
  }

  def loadCueLines(sceneId: SceneId): Try[Seq[CueLine]] = Try{
    loadCueLinesImpl(sceneId)
  }

  def addCueLine(sceneId: SceneId, cueLine: CueLine): Try[Seq[CueLine]] = Try{

    val scene = findScene(sceneId)

    if (!lines.exists{case (_, cl) => cl.cueLineId == cueLine.cueLineId}) {
      lines = lines :+ (scene.id -> cueLine)
    } else {
      saveCueLine(cueLine)
    }

    loadCueLinesImpl(sceneId)
  }

  override def removeCueLine(sceneId: SceneId, cueLineId: CueLineId): Try[Seq[CueLine]] = Try {
    lines = lines.filterNot{case (s, cl) => cl.cueLineId == cueLineId}
    loadCueLinesImpl(sceneId)
  }

  def saveCueLine(cueLine: CueLine): Try[Seq[CueLine]] = Try{
    val linesEntryOpt = lines.find{case(_, cl) => cl.cueLineId == cl.cueLineId}
    linesEntryOpt match {
      case Some((s, cl)) =>
        lines = lines.map(entry => {if (entry._2.cueLineId == cueLine.cueLineId) (entry._1, cueLine) else entry})
        loadCueLinesImpl(s)
      case None =>
        throw new NoSuchCueLineException
    }
  }

  def setCueLines(sceneId: SceneId, newLines: Lines): Try[Seq[CueLine]] = Try {
    lines.filter{case (s, _) => s == sceneId} ++ newLines.lines.map(cl => sceneId -> cl)
    loadCueLinesImpl(sceneId)
  }
  
  def addOrFindUser(emailAddress: String): Try[User] = Try{
    users
      .find(_.email == emailAddress)
      .getOrElse {
        val user = User(UUID.randomUUID().toString, UserEmail(emailAddress))
        users = users :+ user
        user
      }
  }
}

object DbService extends DbService {
  override def loadScenes(user: User): Try[Seq[Scene]] = InMemoryDbService.loadScenes(user)
  override def removeCueLine(scene: Scene, clId: CueLineId): Try[Seq[CueLine]] = InMemoryDbService.removeCueLine(scene, clId)
  override def removeScene(scene: Scene): Try[Scene] = InMemoryDbService.removeScene(scene)
  override def addCueLine(scene: Scene, cl: CueLine): Try[Seq[CueLine]] = InMemoryDbService.addCueLine(scene, cl)
  override def setCueLines(scene: Scene, newLines: Lines): Try[Seq[CueLine]] = InMemoryDbService.setCueLines(scene, newLines)
  override def saveCueLine(cl: CueLine): Try[Seq[CueLine]] = InMemoryDbService.saveCueLine(cl)
  override def loadCueLines(sceneId: SceneId): Try[Seq[CueLine]] = InMemoryDbService.loadCueLines(sceneId)
  override def addScene(user: User, sceneName: String): Try[Seq[Scene]] = InMemoryDbService.addScene(user, sceneName)
  override def renameScene(scene: Scene, newName: String): Try[Scene] = InMemoryDbService.renameScene(scene, newName)
  override def addOrFindUser(email: String): Try[User] = InMemoryDbService.addOrFindUser(email)
}

class AlreadyExistsException(msg: String) extends Exception(msg)
class NoSuchSceneException(sceneId: SceneId) extends Exception(s"Scene ${sceneId.id} doesn't exist.")
class NoSuchUserException(email: String) extends Exception(s"$email is not registered.")
class NoSuchCueLineException extends Exception

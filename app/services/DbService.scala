package services

import java.util.UUID

import model._

import scala.util.Try

trait DbService {

  def loadScenes(user: User): Try[Seq[SceneName]]
  def addScene(user: User, sceneName: String): Try[Seq[SceneName]]
  def removeScene(scene: SceneName): Try[SceneName]
  def renameScene(scene: SceneName, newName: String): Try[SceneName]

  def loadCueLines(scene: SceneName): Try[Seq[CueLine]]
  def addCueLine(scene: SceneName, cl: CueLine): Try[Seq[CueLine]]
  def removeCueLine(scene: SceneName, cueLineId: CueLineId): Try[Seq[CueLine]]
  def saveCueLine(cl: CueLine): Try[Seq[CueLine]]

  def setCueLines(scene: SceneName, newLines: Lines): Try[Seq[CueLine]]

  def addOrFindUser(email: String): Try[User]
}

class SceneNotFoundException(val sceneName: SceneName) extends Exception

object InMemoryDbService extends DbService {

  var scenes = Seq[SceneName]()
  var lines = Seq[(SceneName, CueLine)]()
  var users = Seq[User]()

  def loadScenes(user: User): Try[Seq[SceneName]] = Try { loadScenesImpl(user) }

  def loadScenesImpl(user: User): Seq[SceneName] = {

    if (!users.contains(user)) {
      throw new NoSuchUserException(user.email)
    }

    scenes.filter(_.user == user)
  }


  def addScene(user: User, sceneName: String): Try[Seq[SceneName]] = Try {

    if (scenes.contains(SceneName(user, sceneName))) {
      throw new AlreadyExistsException(s"Scene '$sceneName' already exists.")
    }

    scenes = scenes :+ SceneName(user, sceneName)

    loadScenesImpl(user)
  }

  def removeScene(scene: SceneName): Try[SceneName] = Try {
    if (scenes.contains(scene)) {
      lines = lines.filter(_._1 == scene)
      scenes = scenes.filterNot(_ == scene)
      scene
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

  def loadCueLinesImpl(scene: SceneName): Seq[CueLine] = {
    if (!users.contains(scene.user)) {
      throw new NoSuchUserException(scene.user.email)
    }

    if (!scenes.contains(scene)) {
      throw new NoSuchSceneException(scene.name)
    }

    lines
      .filter{case (s, _) => s == scene}
      .map{case (_, cl) => cl}
  }

  def loadCueLines(scene: SceneName): Try[Seq[CueLine]] = Try{
    loadCueLinesImpl(scene)
  }

  def addCueLine(scene: SceneName, cueLine: CueLine): Try[Seq[CueLine]] = Try{
    if (!scenes.contains(scene)) {
      throw new NoSuchSceneException(scene.name)
    }

    if (!lines.exists{case (_, cl) => cl.cueLineId == cueLine.cueLineId}) {
      lines = lines :+ (scene -> cueLine)
    }
    loadCueLinesImpl(scene)
  }


  override def removeCueLine(scene: SceneName, cueLineId: CueLineId): Try[Seq[CueLine]] = Try {
    lines = lines.filterNot{case (s, cl) => cl.cueLineId == cueLineId}
    loadCueLinesImpl(scene)
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

  def setCueLines(scene: SceneName, newLines: Lines): Try[Seq[CueLine]] = Try {
    lines.filter{case (s, _) => s == scene} ++ newLines.lines.map(cl => scene -> cl)
    loadCueLinesImpl(scene)
  }
  
  def addOrFindUser(emailAddress: String): Try[User] = Try{
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
  override def loadScenes(user: User): Try[Seq[SceneName]] = InMemoryDbService.loadScenes(user)
  override def removeCueLine(scene: SceneName, clId: CueLineId): Try[Seq[CueLine]] = InMemoryDbService.removeCueLine(scene, clId)
  override def removeScene(scene: SceneName): Try[SceneName] = InMemoryDbService.removeScene(scene)
  override def addCueLine(scene: SceneName, cl: CueLine): Try[Seq[CueLine]] = InMemoryDbService.addCueLine(scene, cl)
  override def setCueLines(scene: SceneName, newLines: Lines): Try[Seq[CueLine]] = InMemoryDbService.setCueLines(scene, newLines)
  override def saveCueLine(cl: CueLine): Try[Seq[CueLine]] = InMemoryDbService.saveCueLine(cl)
  override def loadCueLines(scene: SceneName): Try[Seq[CueLine]] = InMemoryDbService.loadCueLines(scene)
  override def addScene(user: User, sceneName: String): Try[Seq[SceneName]] = InMemoryDbService.addScene(user, sceneName)
  override def renameScene(scene: SceneName, newName: String): Try[SceneName] = InMemoryDbService.renameScene(scene, newName)
  override def addOrFindUser(email: String): Try[User] = InMemoryDbService.addOrFindUser(email)
}

class AlreadyExistsException(msg: String) extends Exception(msg)
class NoSuchSceneException(sceneName: String) extends Exception(s"Scene $sceneName doesn't exist.")
class NoSuchUserException(email: String) extends Exception(s"$email is not registered.")
class NoSuchCueLineException extends Exception

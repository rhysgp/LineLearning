package services

import model.CueLine

trait DbService {
  def loadCueLines(userId: String): Seq[CueLine]
  def addCueLine(userId: String, cl: CueLine): Seq[CueLine]
  def saveCueLine(cl: CueLine): Unit
}

object InMemoryDbService extends DbService {

  var userLines = Map[String, Seq[Long]]()
  var lines = Map[Long, CueLine]()

  def loadCueLines(userId: String): Seq[CueLine] = {
    userLines.getOrElse(userId, Seq()).map(lines(_))
  }

  def addCueLine(userId: String, cl: CueLine) = {
    val currentUserAssignments: Seq[Long] = userLines.getOrElse(userId, Seq())

    if (!lines.contains(cl.id)) {
      lines = lines + (cl.id -> cl)
    }

    userLines = userLines + (userId -> (currentUserAssignments :+ cl.id))
    userLines(userId).map(lines)
  }

  def saveCueLine(cl: CueLine): Unit = {
    lines = lines.filterNot(_._1 == cl.id) + (cl.id -> cl)
  }
}

object DbService extends DbService {
  def loadCueLines(userId: String) = InMemoryDbService.loadCueLines(userId)
  def addCueLine(userId: String, cl: CueLine) = InMemoryDbService.addCueLine(userId, cl)
  def saveCueLine(cl: CueLine) = InMemoryDbService.saveCueLine(cl)
}
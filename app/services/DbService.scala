package services

import model.{Lines, CueLine}

trait DbService {
  def loadCueLines(userId: String): Seq[CueLine]
  def addCueLine(userId: String, cl: CueLine): Seq[CueLine]
  def saveCueLine(cl: CueLine): Unit
  def setCueLines(userId: String, lines: Lines): Unit
}

object InMemoryDbService extends DbService {

  var userLines = Map[String, Seq[String]]()
  var lines = Map[String, CueLine]()

  def loadCueLines(userId: String): Seq[CueLine] = {
    userLines.getOrElse(userId, Seq()).map(lines(_))
  }

  def addCueLine(userId: String, cl: CueLine) = {
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

  def setCueLines(userId: String, newLines: Lines): Unit = {
    lines = newLines.lines.map(cl => cl.id -> cl).foldLeft(lines)((map, entry) => {
      map + entry
    })
  }
}

object DbService extends DbService {
  def loadCueLines(userId: String) = InMemoryDbService.loadCueLines(userId)
  def addCueLine(userId: String, cl: CueLine) = InMemoryDbService.addCueLine(userId, cl)
  def saveCueLine(cl: CueLine) = InMemoryDbService.saveCueLine(cl)
  def setCueLines(userId: String, lines: Lines) = InMemoryDbService.setCueLines(userId, lines)
}
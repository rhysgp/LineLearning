package services

import java.util.UUID

import model.{Lines, CueLine}

trait DbService {
  def loadCueLines(userId: User): Seq[CueLine]
  def addCueLine(userId: User, cl: CueLine): Seq[CueLine]
  def saveCueLine(cl: CueLine): Unit
  def setCueLines(userId: User, lines: Lines): Unit

  def addUser(email: String): User
}

object InMemoryDbService extends DbService {

  var userLines = Map[User, Seq[String]]()
  var lines = Map[String, CueLine]()
  var users = Seq[User]()

  def loadCueLines(userId: User): Seq[CueLine] = {
    userLines.getOrElse(userId, Seq()).map(lines(_))
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
  
  def addUser(emailAddress: String): User = {
    val user = User(UUID.randomUUID().toString, emailAddress)
    users = users :+ user
    user
  }
}

object DbService extends DbService {
  def loadCueLines(userId: User) = InMemoryDbService.loadCueLines(userId)
  def addCueLine(userId: User, cl: CueLine) = InMemoryDbService.addCueLine(userId, cl)
  def saveCueLine(cl: CueLine) = InMemoryDbService.saveCueLine(cl)
  def setCueLines(userId: User, lines: Lines) = InMemoryDbService.setCueLines(userId, lines)

  def addUser(email: String) = InMemoryDbService.addUser(email)
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

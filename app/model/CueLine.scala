package model

import java.util.UUID

import services.User

case class SceneName(user: User, name: String) {
  override def toString = {
    s"${user.toString}§$name"
  }
}

object SceneName {
  def fromString(s: String): SceneName = {
    val items = s.split("§")
    SceneName(User.fromString(items(0)), items(1))
  }
}


case class CueLineId(id: String)
object CueLineId {
  def create( ) = CueLineId(UUID.randomUUID().toString)
}

case class CueLine(cueLineId: CueLineId, cue: String, line: String)

case class Lines(nowt: String, lines: Seq[CueLine])

package model

import java.util.UUID

case class SceneId(id: String)
object SceneId { def create() = SceneId(UUID.randomUUID().toString) }

case class Scene(id: SceneId, name: String) {
  override def toString = s"${id.id}§$name"
}

object Scene {
  def fromString(s: String): Scene = {
    val items = s.split("§")
    Scene(SceneId(items(0)), items(1))
  }
}


case class CueLineId(id: String)
object CueLineId { def create( ) = CueLineId(UUID.randomUUID().toString) }

case class CueLine(cueLineId: CueLineId, cue: String, line: String)

case class Lines(nowt: String, lines: Seq[CueLine])

package model

import java.util.UUID

import db.DbData

case class SceneId(id: String)
object SceneId { def create() = SceneId(UUID.randomUUID().toString) }

case class Scene(id: SceneId, name: String) {
  override def toString = s"${id.id}"
}

case class CueLineId(id: String)
object CueLineId { def create( ) = CueLineId(UUID.randomUUID().toString) }

case class CueLine(cueLineId: CueLineId, cue: String, line: String)

case class Lines(nowt: String, lines: Seq[CueLine])

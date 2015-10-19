package model

import services.User

case class SceneName(user: User, name: String)
case class CueLineId(id: String)

case class CueLine(CueLineId: String, cue: String, line: String)

case class Lines(nowt: String, lines: Seq[CueLine])

package model

case class CueLine(id: String, cue: String, line: String)

case class Lines(nowt: String, lines: Seq[CueLine])

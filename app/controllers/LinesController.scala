package controllers

import model.{CueLine, Lines}
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._
import services.DbService
import support.CookieHelper._

class LinesController extends Controller {

  def lines() = Action {

    implicit request =>

      request.cookies.get(COOKIE_NAME) match {

        case Some(cookie) =>
          Ok(views.html.list(DbService.loadCueLines(cookie.value)))

        case None =>
          Unauthorized("Don't know who you are.")
      }

  }



  def saveLines = Action(BodyParsers.parse.json) { request =>

    val lines = request.body.validate[Lines]

    DbService.saveCueLine()

    Ok("")
  }

  implicit val readCueLine: Reads[CueLine] = (
    (JsPath \ "id").read[Long] and (JsPath \ "cue").read[String] and (JsPath \ "line").read[String]
  )(CueLine.apply _)

  implicit val readLines: Reads[Lines] = (
    (JsPath \ "nowt").read[String] and (JsPath \ "lines").read[Seq[CueLine]]
  )(Lines.apply _)

}

package controllers

import model.{CueLine, Lines}
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._
import services.DbService
import support.CookieHelper._


/*
 * To sign up:
 *     1. Enter email address.
 *     2. System either:
 *          a) finds an account and sends the account GUID to the email
 *          b) creates a new account (with a new GUID) and sends an email
 *
 *
 */

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

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        request.body.validate[Lines].asOpt match {

          case Some(lines) =>
            DbService.setCueLines("", lines)
            Ok(views.html.list(DbService.loadCueLines(cookie.value)))

          case None =>
            BadRequest("No lines found")
        }

      case None =>
        Unauthorized("Don't know who you are.")
    }
  }

  implicit val readCueLine: Reads[CueLine] = (
    (JsPath \ "id").read[String] and (JsPath \ "cue").read[String] and (JsPath \ "line").read[String]
  )(CueLine.apply _)

  implicit val readLines: Reads[Lines] = (
    (JsPath \ "nowt").read[String] and (JsPath \ "lines").read[Seq[CueLine]]
  )(Lines.apply _)

}

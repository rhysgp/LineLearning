package controllers

import play.api.mvc._


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

//  def lines() = Action {
//
//    implicit request =>
//
//      request.cookies.get(COOKIE_NAME) match {
//
//        case Some(cookie) =>
//
//          val user = User.fromString(cookie.value)
//          DbService.loadCueLines(SceneName(user, "dummy")) match {
//
//            case Success(lines) =>
//              Ok(views.html.list(lines))
//
//            case Failure(t) =>
//              BadRequest(views.html.error(t))
//
//          }
//
//
//
//        case None =>
//          Unauthorized("Don't know who you are.")
//      }
//  }
//
//  def saveLines = Action(BodyParsers.parse.json) { request =>
//
//    request.cookies.get(COOKIE_NAME) match {
//
//      case Some(cookie) =>
//        request.body.validate[Lines].asOpt match {
//
//          case Some(lines) =>
//
//            val user = User.fromString(cookie.value)
//            DbService.loadCueLines(SceneName(user, "dummy")) match {
//
//              case Success(cueLines) =>
//                Ok(views.html.list(cueLines))
//
//              case Failure(t) =>
//                BadRequest(views.html.error(t))
//            }
//
//          case None =>
//            BadRequest("No lines found")
//        }
//
//      case None =>
//        Unauthorized("Don't know who you are.")
//    }
//  }
//
//  implicit val readCueLineId: Reads[CueLineId] = ((JsPath \ "id").read[String])(CueLineId.apply _)
//
//
//  implicit val readCueLine: Reads[CueLine] = (
//    (JsPath \ "id").read[CueLineId] and (JsPath \ "cue").read[String] and (JsPath \ "line").read[String]
//  )(CueLine.apply _)
//
//  implicit val readLines: Reads[Lines] = (
//    (JsPath \ "nowt").read[String] and (JsPath \ "lines").read[Seq[CueLine]]
//  )(Lines.apply _)

}

package controllers

import java.util.UUID

import model.{CueLineId, SceneName, CueLine}
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{Action, _}
import services.{User, DbService}
import support.CookieHelper._

import scala.util.{Failure, Success}

class PromptController extends Controller {

  def index = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

//        val user = User.fromString(cookie.value)
//        val lines = DbService.loadCueLines(user)
//
//        if (lines.nonEmpty) {
//          Ok(views.html.prompt(0, lines.head.cue, lines.head.line))
//        } else {
//          Redirect(routes.PromptController.edit())
//        }
        Ok("Not implemented")

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def line(index: Int) = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user = User.fromString(cookie.value)

        DbService.loadCueLines(SceneName(User("dummy", "dummy"), "dummy")) match {
          case Success(lines) =>
            if (index >= 0 && index < lines.length) {
              val cueLine = lines(index)
              Ok(views.html.prompt(index, cueLine.cue, cueLine.line))
            } else {
              Redirect(routes.PromptController.line(0))
            }

          case Failure(t) =>
            Ok("Failed to load cue lines...")
        }


      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def list(sceneStream: String) = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val scene = SceneName.fromString(sceneStream)
        DbService.loadCueLines(scene) match {
          case Success(lines) =>
            Ok(views.html.list(lines))

          case Failure(t) =>
            Ok("Failed to load cue lines...")
        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def edit = Action { request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)

        DbService.loadCueLines(SceneName(User("dummy", "dummy"), "dummy")) match {
          case Success(lines) =>
            Ok(views.html.add(addForm, lines))

          case Failure(t) =>
            Ok("Failed to load cue lines...")
        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }
  
  def add  = Action { request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)

        addForm.bindFromRequest()(request).fold(
          formWithErrors => {


            DbService.loadCueLines(SceneName(User("dummy", "dummy"), "dummy")) match {
              case Success(lines) =>
                BadRequest(views.html.add(formWithErrors, lines))

              case Failure(t) =>
                Ok("Failed to load cue lines...")
            }

          },
          formData => {

            val scene = SceneName(User("dummy", "dummy"), "dummy")
            DbService.addCueLine(scene, CueLine(CueLineId.create(), formData.cue, formData.line))
            DbService.loadCueLines(scene) match {
              case Success(lines) =>
                Ok(views.html.add(addForm, lines))

              case Failure(t) =>
                Ok("Failed to load cue lines...")
            }


          }
        )

      case None =>
        Redirect(routes.UserController.register())
    }

  }

  val addForm = Form(
    mapping(
      "cue" -> nonEmptyText,
      "line" -> nonEmptyText
    )(AddCueLine.apply)(AddCueLine.unapply)
  )

}

case class AddCueLine(cue: String, line: String)
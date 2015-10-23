package controllers

import model.{CueLine, CueLineId, SceneName}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{Action, _}
import services.{DbService, User}
import support.CookieHelper._

import scala.util.{Failure, Success}

class PromptController extends Controller {

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
            Ok(views.html.cueLines(scene, lines, addForm))

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
            val scene = SceneName.fromString(formData.sceneStream)
            DbService.addCueLine(scene, CueLine(CueLineId.create(), formData.cue, formData.line))
            DbService.loadCueLines(scene) match {
              case Success(lines) =>
                Redirect(routes.PromptController.list(scene.toString))

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
      "sceneStream" -> nonEmptyText,
      "cue" -> nonEmptyText,
      "line" -> nonEmptyText
    )(AddCueLine.apply)(AddCueLine.unapply)
  )

}

case class AddCueLine(sceneStream: String, cue: String, line: String)
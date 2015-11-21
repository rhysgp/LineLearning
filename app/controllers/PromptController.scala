package controllers

import model._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import services.DbService
import support.CookieHelper._
import views.NavigationHelper._

import scala.util.{Failure, Success}

class PromptController extends Controller {

  def line(sceneId: String, index: Int) = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user = User.fromString(cookie.value)

        DbService.loadCueLines(SceneId(sceneId)) match {
          case Success(lines) =>
            if (index >= 0 && index < lines.length) {
              val cueLine = lines(index)
              DbService.scene(SceneId(sceneId)) match {
                case Success(scene) =>
                  Ok(views.html.prompt(buildNavigation(Option(user)), scene, index, cueLine.cue, cueLine.line))
                case Failure(t) =>
                  Ok(t.getMessage)
              }
            } else {
              Redirect(routes.PromptController.line(sceneId, 0))
            }

          case Failure(t) =>
            Ok("Failed to load cue lines...")
        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def list(sceneId: String) = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user = User.fromString(cookie.value)
        DbService.scene(SceneId(sceneId)) match {
          case Success(scene) =>
            DbService.loadCueLines(SceneId(sceneId)) match {
              case Success(lines) =>
                val navigation = buildNavigation(Option(user), sceneName = Option(scene))
                Ok(views.html.cueLines(navigation, scene, lines, addForm))

              case Failure(t) =>
                Ok("Failed to load cue lines...")
            }

          case Failure(t) =>
            Ok(t.getMessage)
        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def edit(sceneId: String, index: Int) = Action { request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)

        DbService.loadCueLines(SceneId(sceneId)) match {
          case Success(lines) =>
            Ok(views.html.add(buildNavigation(Option(user)), addForm, lines))

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
            Ok(formWithErrors.toString)
          },
          formData => {

            DbService.scene(SceneId(formData.sceneId)) match {
              case Success(scene) =>
                if (formData.cueLineId.length > 0) {
                  val modifiedCl = CueLine(CueLineId(formData.cueLineId), formData.cue, formData.line)
                  DbService.saveCueLine(modifiedCl) match {
                    case Success(cueLines) =>
                      Redirect(routes.PromptController.list(scene.toString))
                        .flashing("failed" -> "Error!")
                    case Failure(t) =>
                      Redirect(routes.PromptController.list(scene.toString))
                        .flashing("failed" -> t.getMessage)
                  }
                } else {
                  DbService.addCueLine(scene.id, CueLine(CueLineId.create(), formData.cue, formData.line)) match {
                    case Success(lines) =>
                      Redirect(routes.PromptController.list(scene.toString))

                    case Failure(t) =>
                      Ok("Failed to load cue lines...")
                  }
                }

              case Failure (t) =>
                Ok("Failed to load cue lines...")
            }
          }
        )

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def delete() = Action { request =>
    deleteForm.bindFromRequest()(request).fold(
      formWithErrors => {
        Redirect(routes.ScenesController.list)
          .flashing("failed" -> "Delete failed")
      },
      formData => {
        val sceneId = SceneId(formData.sceneId)
        DbService.removeCueLine(sceneId, CueLineId(formData.cueLineId)) match {
          case Success(_) =>
            Redirect(routes.PromptController.list(formData.sceneId))
          case Failure(t) =>
            Redirect(routes.PromptController.list(formData.sceneId))
              .flashing("failed" -> t.getMessage)
        }
      }
    )
  }

  val addForm = Form(
    mapping(
      "sceneId" -> nonEmptyText,
      "cueLineId" -> text,
      "cue" -> nonEmptyText,
      "line" -> nonEmptyText
    )(AddCueLine.apply)(AddCueLine.unapply)
  )

  val deleteForm = Form(
    mapping(
      "sceneId" -> nonEmptyText,
      "cueLineId" -> nonEmptyText
    )(DeleteCueLine.apply)(DeleteCueLine.unapply)
  )
}

case class AddCueLine(sceneId: String, cueLineId: String, cue: String, line: String)
case class DeleteCueLine(sceneId: String, cueLineId: String)

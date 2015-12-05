package controllers

import java.util.UUID
import javax.inject.Inject

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import db._
import db.Conversions._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import services.{DbServiceAsync, DbService}
import support.CookieHelper._
import views.NavigationHelper._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PromptController @Inject() (dbService: DbServiceAsync) extends Controller {

  def line(sceneId: String, index: Int) = Action.async { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user: User = cookie.value

        dbService.loadCueLines(sceneId)
          .map{ lines =>
            if (index >= 0 && index < lines.length) {
              val cueLine = lines(index)

              dbService.scene(sceneId).map { scene =>
                Ok(views.html.prompt(buildNavigation(Option(user)), scene, index, cueLine.cue, cueLine.line))
              }
            } else {
              Redirect(routes.PromptController.line(sceneId, 0))
            }
          }

      case None =>
        Future(Redirect(routes.UserController.register()))
    }
  }

  def list(sceneId: String) = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user: User = cookie.value
        dbService.scene(sceneId) match {
          case Success(scene) =>
            dbService.loadCueLines(sceneId) match {
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

        val user: User = cookie.value

        dbService.loadCueLines(sceneId) match {
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

        val user: User = cookie.value

        addForm.bindFromRequest()(request).fold(
          formWithErrors => {
            Ok(formWithErrors.toString)
          },
          formData => {

            dbService.scene(formData.sceneId) match {
              case Success(scene) =>
                if (formData.cueLineId.length > 0) {
                  val modifiedCl = CueLine(formData.cueLineId, formData.cue, formData.line)
                  dbService.saveCueLine(modifiedCl) match {
                    case Success(cueLines) =>
                      Redirect(routes.PromptController.list(scene.toString))
                        .flashing("failed" -> "Error!")
                    case Failure(t) =>
                      Redirect(routes.PromptController.list(scene.toString))
                        .flashing("failed" -> t.getMessage)
                  }
                } else {
                  dbService.addCueLine(scene.id, CueLine(UUID.randomUUID().toString, formData.cue, formData.line)) match {
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
        Redirect(routes.ScenesController.list())
          .flashing("failed" -> "Delete failed")
      },
      formData => {
        val sceneId = formData.sceneId
        dbService.removeCueLine(sceneId, formData.cueLineId) match {
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

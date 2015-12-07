package controllers

import java.util.UUID
import javax.inject.Inject

import db.Conversions._
import db._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.DbServiceAsync
import support.CookieHelper._
import views.NavigationHelper._

import scala.concurrent.Future

class PromptController @Inject() (dbService: DbServiceAsync) extends Controller {

  def line(sceneId: String, index: Int) = Action.async { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user: User = cookie.value

        dbService.loadCueLines(sceneId)
          .flatMap{ lines =>
            if (index >= 0 && index < lines.length) {
              val cueLine = lines(index)

              dbService.scene(sceneId).map { scene =>
                Ok(views.html.prompt(buildNavigation(Option(user)), scene, index, cueLine.cue, cueLine.line))
              }
            } else {
              Future(Redirect(routes.PromptController.line(sceneId, 0)))
            }
          }

      case None =>
        Future(Redirect(routes.UserController.register()))
    }
  }

  def list(sceneId: String) = Action.async { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user: User = cookie.value

        dbService.scene(sceneId).flatMap{ scene =>
          dbService.loadCueLines(sceneId).map { lines =>
            val navigation = buildNavigation(Option(user), sceneName = Option(scene))
            Ok(views.html.cueLines(navigation, scene, lines, addForm))
          }
        }

      case None =>
        Future(Redirect(routes.UserController.register()))
    }
  }

  def edit(sceneId: String, index: Int) = Action.async { request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user: User = cookie.value

        dbService.loadCueLines(sceneId).map{ lines =>
          Ok(views.html.add(buildNavigation(Option(user)), addForm, lines))
        }

      case None =>
        Future(Redirect(routes.UserController.register()))
    }
  }
  
  def add  = Action.async { request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user: User = cookie.value

        addForm.bindFromRequest()(request).fold(
          formWithErrors => {
            Future(Ok(formWithErrors.toString))
          },
          formData => {

            dbService.scene(formData.sceneId).flatMap { scene =>
              if (formData.cueLineId.length > 0) {
                val modifiedCl = CueLine(formData.cueLineId, formData.cue, formData.line)
                dbService.saveCueLine(modifiedCl).map { lines =>
                  Redirect(routes.PromptController.list(scene.toString))
                }
              } else {
                val newCl = CueLine(UUID.randomUUID().toString, formData.cue, formData.line)
                dbService.addCueLine(scene.id, newCl).map{ lines =>
                  Redirect(routes.PromptController.list(scene.toString))
                }
              }
            }
          }
        )

      case None =>
        Future(Redirect(routes.UserController.register()))
    }
  }

  def delete() = Action.async { request =>
    deleteForm.bindFromRequest()(request).fold(
      formWithErrors => {
        Future(Redirect(routes.ScenesController.list())
          .flashing("failed" -> "Delete failed"))
      },
      formData => {
        val sceneId = formData.sceneId
        dbService.removeCueLine(sceneId, formData.cueLineId).map { lines =>
            Redirect(routes.PromptController.list(formData.sceneId))
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

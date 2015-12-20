package controllers

import javax.inject.Inject

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import db.Conversions._
import db.User
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{Action, _}
import services.DbServiceAsync
import support.CookieHelper._
import views.NavigationHelper._

import scala.concurrent.Future


class ScenesController @Inject() (dbService: DbServiceAsync) extends Controller {

  def list() = Action.async { implicit request =>
    request.cookies.get(COOKIE_NAME) match {
      case Some(cookie) =>
        println(cookie.value)
        val user: User = cookie.value
        println(user)
        dbService.loadScenes(user).map{ scenes =>
          Ok(views.html.scenes(buildNavigation(Option(user)), scenes, sceneForm))
        } recover {
          case t =>
            Redirect(routes.UserController.registerPost()).flashing("failure" -> t.getMessage)
        }
      case None =>
        Future(Redirect(routes.UserController.registerPost()))
    }
  }

  def addScene() = Action.async { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user: User = cookie.value

        sceneForm.bindFromRequest.fold(
          formWithErrors => {
            Future(Redirect(routes.ScenesController.list())
              .flashing("failure" -> formWithErrors.errors.mkString(". ")))
          },
          sceneData => {
            dbService.addScene(user, sceneData.sceneName)
              .map(_ => Redirect(routes.ScenesController.list()))
              .recover{
                case t if t.getMessage.contains("Unique index or primary key violation") =>
                  Redirect(routes.ScenesController.list()).flashing("failure" -> s"I think a scene with name '${sceneData.sceneName}' already exists.")
                case t =>
                  Redirect(routes.ScenesController.list()).flashing("failure" -> t.getMessage)
              }

          }
        )

      case None =>
        Future(Redirect(routes.UserController.registerPost()))
    }
  }

  def delete() = Action.async { implicit request =>
    request.cookies.get(COOKIE_NAME) match {
      case Some(cookie) =>
        val user: User = cookie.value
        deleteSceneForm.bindFromRequest.fold(
          formWithErrors => {
            Future(Redirect(routes.ScenesController.list())
              .flashing("failure" -> formWithErrors.errors.mkString(". ")))
          },
          deleteSceneData => {
            dbService.removeScene(user, deleteSceneData.sceneId)
              .map(scene => Redirect(routes.ScenesController.list()))
          }
        )
      case None =>
        Future(Redirect(routes.UserController.registerPost()))
    }
  }

  val sceneForm = Form(
    mapping(
      "sceneName" -> nonEmptyText
    )(SceneFormData.apply)(SceneFormData.unapply)
  )

  val deleteSceneForm = Form(
    mapping(
      "sceneId" -> nonEmptyText
    )(DeleteScene.apply)(DeleteScene.unapply)
  )
}

case class SceneFormData(sceneName: String)
case class DeleteScene(sceneId: String)

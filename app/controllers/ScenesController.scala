package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{Action, _}
import services.{User, DbService}
import support.CookieHelper._

import scala.util.{Failure, Success}


class ScenesController extends Controller {

  def list = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)

        DbService.loadScenes(user) match {

          case Success(scenes) =>
            Ok(views.html.scenes(scenes, sceneForm))

          case Failure(t) =>
            BadRequest(views.html.error(t))

        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def addScene = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user = User.fromString(cookie.value)

        sceneForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest(views.html.scenes(loadScenes(user), formWithErrors))
          },
          sceneData => {
            DbService.addScene(user, sceneData.sceneName)
            Redirect(routes.ScenesController.list())
//            Ok(views.html.scenes(loadScenes(user), sceneForm))
          }
        )

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  private def loadScenes(user: User) =
    DbService.loadScenes(user) match {
      case Success(s) => s
      case Failure(t) => Seq()
    }

  val sceneForm = Form(
    mapping(
      "sceneName" -> nonEmptyText
    )(SceneFormData.apply)(SceneFormData.unapply)
  )

}

case class SceneFormData(sceneName: String)


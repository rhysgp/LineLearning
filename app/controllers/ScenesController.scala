package controllers

import model.{SceneId, User}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{Action, _}
import services.{AlreadyExistsException, DbService, NoSuchUserException}
import support.CookieHelper
import support.CookieHelper._
import views.NavigationHelper._

import scala.util.{Failure, Success}


class ScenesController extends Controller {

  def list = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)

        DbService.loadScenes(user) match {

          case Success(scenes) =>
            Ok(views.html.scenes(buildNavigation(Option(user)), scenes, sceneForm))

          case Failure(t) if t.isInstanceOf[NoSuchUserException] =>
            BadRequest(views.html.error(buildNavigation(Option(user)), t))
              .discardingCookies(DiscardingCookie(CookieHelper.COOKIE_NAME))

          case Failure(t) =>
            BadRequest(views.html.error(buildNavigation(Option(user)), t))

        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def addScene() = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user = User.fromString(cookie.value)

        sceneForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest(views.html.scenes(buildNavigation(Option(user)), loadScenes(user), formWithErrors))
          },
          sceneData => {
            DbService.addScene(user, sceneData.sceneName) match {
              case Success(_) =>
                Redirect(routes.ScenesController.list())
              case Failure(t) if t.isInstanceOf[AlreadyExistsException] =>
                Redirect(routes.ScenesController.list()).flashing("error" -> t.getMessage)
              case Failure(t) =>
                InternalServerError(views.html.error(buildNavigation(Option(user)), t))
            }
          }
        )

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def delete = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)

        deleteSceneForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest(views.html.scenes(buildNavigation(Option(user)), loadScenes(user), sceneForm))
          },
          deleteSceneData => {
            val user = User.fromString(cookie.value)

            DbService.removeScene(SceneId(deleteSceneData.sceneId)) match {

              case Success(scenes) =>
                Redirect(routes.ScenesController.list)

              case Failure(t) =>
                BadRequest(views.html.error(buildNavigation(Option(user)), t))

            }

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

  val deleteSceneForm = Form(
    mapping(
      "sceneStream" -> nonEmptyText
    )(DeleteScene.apply)(DeleteScene.unapply)
  )
}

case class SceneFormData(sceneName: String)
case class DeleteScene(sceneId: String)

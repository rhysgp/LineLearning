package controllers

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
            Ok(views.html.scenes(scenes))

          case Failure(t) =>
            BadRequest(views.html.error(t))

        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }
}

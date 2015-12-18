package controllers

import play.api.mvc._
import support.CookieHelper._

class Application extends Controller {

  def index = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        Redirect(routes.ScenesController.list())

      case None =>
        Redirect(routes.UserController.login())
    }

  }

}

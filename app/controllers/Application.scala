package controllers

import db.User
import play.api.mvc._
import support.CookieHelper._
import views.NavigationHelper._
import db.Conversions._


class Application extends Controller {

  def index() = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        Redirect(routes.ScenesController.list())

      case None =>
        Redirect(routes.UserController.login())
    }

  }

  def home() = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user: User = cookie.value
        Ok(views.html.home(buildNavigation(Option(user), showSceneNav = true)))

      case None =>
        Ok(views.html.homeWithoutLogin(noNavigation))

    }
  }

}

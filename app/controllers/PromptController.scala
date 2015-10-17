package controllers

import play.api.mvc.{Action, _}
import services.{User, DbService}
import support.CookieHelper._

class PromptController extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def list = Action { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        Ok(views.html.list(DbService.loadCueLines(User.fromString(cookie.value))))

      case None =>
        Unauthorized("Don't know who you are.")
    }
  }

  def add = Action {
    Ok(views.html.add())
  }
}

package controllers

import java.util.UUID

import model.CueLine
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{Action, _}
import services.{User, DbService}
import support.CookieHelper._

class PromptController extends Controller {

  def index = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)
        val lines = DbService.loadCueLines(user)

        if (lines.nonEmpty) {
          Ok(views.html.prompt(0, lines.head.cue, lines.head.line))
        } else {
          Redirect(routes.PromptController.edit())
        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def line(index: Int) = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user = User.fromString(cookie.value)
        val lines = DbService.loadCueLines(user)

        if (index >= 0 && index < lines.length) {
          val cueLine = lines(index)
          Ok(views.html.prompt(index, cueLine.cue, cueLine.line))
        } else {
          Redirect(routes.PromptController.index())
        }

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def list = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        Ok(views.html.list(DbService.loadCueLines(User.fromString(cookie.value))))

      case None =>
        Redirect(routes.UserController.register())
    }
  }

  def edit = Action { request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>

        val user = User.fromString(cookie.value)
        val lines = DbService.loadCueLines(user)

        Ok(views.html.add(addForm, lines))

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
            BadRequest(views.html.add(formWithErrors, DbService.loadCueLines(user)))
          },
          formData => {
            DbService.addCueLine(user, CueLine(UUID.randomUUID().toString, formData.cue, formData.line))
            Ok(views.html.add(addForm, DbService.loadCueLines(user)))
          }
        )

      case None =>
        Redirect(routes.UserController.register())
    }

  }

  val addForm = Form(
    mapping(
      "cue" -> nonEmptyText,
      "line" -> nonEmptyText
    )(AddCueLine.apply)(AddCueLine.unapply)
  )

}

case class AddCueLine(cue: String, line: String)
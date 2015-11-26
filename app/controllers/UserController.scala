package controllers

import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc._
import services.DbServiceAsync
import support.CookieHelper

import views.NavigationHelper._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class UserController @Inject()(val messagesApi: MessagesApi, val dbService: DbServiceAsync) extends Controller with I18nSupport {

  def index() = Action { implicit request =>
    Ok(views.html.register(noNavigation, registerForm))
  }

  def register() = Action.async { implicit request =>
    registerForm.bindFromRequest.fold(
      formWithErrors =>
        Future(BadRequest(views.html.register(noNavigation, formWithErrors))),
      registerData   => {

        // actually do the register:

        //  a) Could check email address existence and error if it doesn't exist
        //  b) If email address OK, check to see if it is an existing user, and either:
        //     i) Find the existing user's GUID and send them an email; OR
        //    ii) Create a new user, generating the GUID, and send them an email

        dbService.addOrFindUser(registerData.email)
          .map { user =>
            Redirect(routes.ScenesController.list())
              .withCookies(Cookie(CookieHelper.COOKIE_NAME, user.toString))
          }
          .recover {
            case e: Exception => Redirect(routes.UserController.index()).flashing("failure" -> e.getMessage)
          }
      }
    )
  }

  val registerForm = Form(
    mapping(
      "email" -> email
    )(Register.apply)(Register.unapply)
  )
}

case class Register(email: String)

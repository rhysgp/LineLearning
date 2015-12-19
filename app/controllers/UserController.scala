package controllers

import javax.inject.Inject

import db.Conversions._
import db.User
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.DbServiceAsync
import support.CookieHelper
import support.CookieHelper._
import views.NavigationHelper._

import scala.concurrent.Future

import services.mail._

class UserController @Inject()(val messagesApi: MessagesApi, val dbService: DbServiceAsync) extends Controller with I18nSupport {

  def index() = Action { implicit request =>
    Ok(views.html.register(noNavigation, registerForm))
  }

  def register() = Action.async { implicit request =>

    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        Future(Redirect(routes.Application.index()))

      case None =>
        registerForm.bindFromRequest.fold(
          formWithErrors =>
            Future(BadRequest(views.html.register(noNavigation, formWithErrors))),

          registerData   => {

            // actually do the register:

            //  a) Could check email address existence and error if it doesn't exist
            //  b) If email address OK, check to see if it is an existing user, and either:
            //     i) Find the existing user's GUID and send them an email; OR
            //    ii) Create a new user, generating the GUID, and send them an email


            // create the email from the template:

            dbService.addOrFindUser(registerData.email)
              .map { user =>
                send a Mail (
                  from = "line-learning@rhyssoft.com" -> "Line Learning",
                  to = user.email,
                  cc = "line-learning@rhyssoft.com",
                  subject = "RhysSoft Line Learning Registration",
                  message = views.html.email.register_text(user).toString,
                  richMessage = Some(views.html.email.register(user).toString)
                )
                user
              }
              .map { user =>
                Redirect(routes.ScenesController.list())
                  .withCookies(Cookie(CookieHelper.COOKIE_NAME, user.toString))
              }
              .recover {
                case e: Exception =>
                  Logger.error(e.getMessage, e)
                  Redirect(routes.UserController.index()).flashing("failure" -> e.getMessage)
              }
          }
        )
    }
  }

  def login = Action { implicit request =>
    request.cookies.get(COOKIE_NAME) match {
      case Some(_) =>
        Redirect(routes.Application.index())
      case None =>
        Ok(views.html.login(loginForm, buildNavigation(None, showSceneNav = false)))
          .discardingCookies(DiscardingCookie(CookieHelper.COOKIE_NAME))
    }
  }

  def loginPost() = Action.async { implicit request =>

    loginForm.bindFromRequest().fold(
      formWithErrors =>
        Future(Ok(views.html.login(formWithErrors, buildNavigation(None)))),

      loginData =>
        dbService.findUser(loginData.email, loginData. password)
          .map(user => {
            Redirect(routes.Application.index())
              .withCookies(Cookie(CookieHelper.COOKIE_NAME, user.toString))
          })
          .recover{ case _ =>
            val form = loginForm.fill(loginData.copy(password = ""))
            Ok(views.html.login(form, buildNavigation(None), loginFailed = true))
          }
    )
  }

  def passwordChange = Action{ implicit request =>
    request.cookies.get(COOKIE_NAME) match {

      case Some(cookie) =>
        val user: User = cookie.value
        Ok(views.html.password(passwordChangeForm, buildNavigation(Option(user))))

      case None =>
        Redirect(routes.Application.index())
    }
  }

  def passwordChangePost() = Action.async { implicit request =>

    request.cookies.get(COOKIE_NAME) match {
      case Some(cookie) =>
        val user: User = cookie.value
        passwordChangeForm.bindFromRequest().fold(
          formWithErrors =>
            Future(Ok(views.html.password(formWithErrors, buildNavigation(Option(user))))),
          formData =>

            // CHECK THAT THE TWO NEW PASSWORDS MATCH!!! - OR DO THIS IN THE UI!!!

            // TODO - check to see if there's a way of validating this using the Form mapping

            if (formData.newPassword != formData.reTypedNewPassword) {
              val frm = passwordChangeForm.fill(formData).withError("retyped_new_password", "New passwords don't match")
              Future(Ok(views.html.password(frm, buildNavigation(Option(user)))))
            } else {
              dbService.changePassword(user.email, formData.oldPassword, formData.newPassword)
                .map(x => Redirect(routes.Application.index()))
                .recover{ case t =>
                  Logger.error(t.getMessage, t)
                  Ok(views.html.password(passwordChangeForm, buildNavigation(Option(user)), failed = true))
                }
            }
        )
      case None =>
        Future(Redirect(routes.Application.index()))
    }
  }


//  val passwordCheckConstraint: Constraint[String] = Constraint("constraints.passwordcheck")({
//    plainText =>
//      val errors = plainText match {
//        case allNumbers() => Seq(ValidationError("Password is all numbers"))
//        case allLetters() => Seq(ValidationError("Password is all letters"))
//        case _ => Nil
//      }
//      if (errors.isEmpty) {
//        Valid
//      } else {
//        Invalid(errors)
//      }
//  })

  val registerForm = Form(
    mapping(
      "email" -> email
    )(Register.apply)(Register.unapply)
  )

  val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(Login.apply)(Login.unapply)
  )

  val passwordChangeForm = Form(
    mapping(
      "old_password" -> nonEmptyText,
      "new_password" -> nonEmptyText(minLength = 6),
      "retyped_new_password" -> nonEmptyText(minLength = 6)
    )(PasswordChange.apply)(PasswordChange.unapply)//.verifying(pw => pw.newPassword == pw.oldPassword)
  )
}

case class Register(email: String)
case class Login(email: String, password: String)
case class PasswordChange(oldPassword: String, newPassword: String, reTypedNewPassword: String)
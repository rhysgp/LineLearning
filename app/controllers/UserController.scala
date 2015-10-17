package controllers

import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc._
import services.{User, DbService}

class UserController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def index() = Action { implicit request =>
    Ok(views.html.register(registerForm))
  }

  def register() = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.register(formWithErrors)),
      registerData   => {

        // actually do the register:

        //  a) Could check email address existence and error if it doesn't exist

        //  b) If email address OK, check to see if it is an existing user, and either:
        //    i) Find the existing user's GUID and send them an email; OR
        //   ii) Create a new user, generating the GUID, and send them an email

        val user = DbService.addOrFindUser(registerData.email)

        Ok(views.html.lines(DbService.loadCueLines(user)))
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

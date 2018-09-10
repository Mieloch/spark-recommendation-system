package controllers

import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import models.UserData
import models.dao.UserRepository
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               messagesAction: MessagesActionBuilder,
                               userRepository: UserRepository)
                              (implicit ec: ExecutionContext)
  extends AbstractController(cc) with LazyLogging {

  def login(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    userForm.bindFromRequest.fold(
      _ => Future(BadRequest("Form has errors")),
      userData => userRepository.findByUserNameAndPassword(userData.name, userData.password)
        .map {
          case Some(_) =>
            Redirect(routes.RecommendationController.recommendation())
              .withSession(
                AuthenticationAction.USER_SESSION_NAME -> userData.name
              )
          case None => BadRequest("Unable to login")
        }
        .recover { case ex => BadRequest(s"Unable to login ${ex.getMessage}") }
    )
  }
  }

  def register(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    userForm.bindFromRequest().fold(
      _ => Future(BadRequest("Form errors")),
      userData => userRepository.findByUserName(userData.name)
        .map {
          case Some(_) => Future(BadRequest("Unable to create user"))
          case None =>
            userRepository
              .add(userData)
              .map(_ => Redirect(routes.RecommendationController.recommendation())
              .withSession(AuthenticationAction.USER_SESSION_NAME -> userData.name))

        }.flatMap(identity)
    )
  }
  }

  def loginForm() = messagesAction {
    implicit request: MessagesRequest[AnyContent] =>
      Ok(views.html.login(userForm))
  }

  def registerForm() = messagesAction {
    implicit request: MessagesRequest[AnyContent] =>
      Ok(views.html.register(userForm))
  }


  val userForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserData.apply)(UserData.unapply)
  )
}

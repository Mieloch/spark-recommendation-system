package controllers

import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    request.session.get(AuthenticationAction.USER_SESSION_NAME) match {
      case Some(_) => block(request)
      case None => Future.successful(Forbidden("U have to login first"))
    }
  }

}

object AuthenticationAction{
  val USER_SESSION_NAME = "user"
}

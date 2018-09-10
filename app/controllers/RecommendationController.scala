package controllers

import javax.inject.Inject
import models.dao.UserRepository
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.recommendation.RecommendationEngineFactory

import scala.concurrent.{ExecutionContext, Future}

class RecommendationController @Inject()(cc: ControllerComponents,
                                         recommendationEngineFactory: RecommendationEngineFactory,
                                         userRepository: UserRepository,
                                         authenticationAction: AuthenticationAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def recommendation() = authenticationAction.async { implicit request: Request[AnyContent] => {
    val engine = recommendationEngineFactory
      .getEngine()
    val userName = request.session.get(AuthenticationAction.USER_SESSION_NAME).get
    val user = userRepository.findByUserName(userName)
    user.flatMap {
      case Some(u) =>
        val userId = u.id.toInt
        engine
          .top10RatingsFromAll(userId)
          .zip(engine.top10RatingsFromMostPopular(userId))
          .map { case (topRatingsFromAll, topRatingsFromMostPopular) =>
            Ok(views.html.recommendation(userName, topRatingsFromAll, topRatingsFromMostPopular))
          }
          .recover { case ex => InternalServerError(s"Unable to find recommendations. ${ex.getMessage}") }
      case None => Future.successful(Ok("aa"))
    }
  }
  }


}

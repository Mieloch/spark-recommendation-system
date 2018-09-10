package controllers

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import models.dao.{RatingRepository, UserRepository}
import models.{Rating, RatingListData}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services.recommendation.RecommendationEngineFactory

import scala.concurrent.{ExecutionContext, Future}

class RatingsController @Inject()(authenticationAction: AuthenticationAction,
                                  cc: MessagesControllerComponents,
                                  recommendationEngineFactory: RecommendationEngineFactory,
                                  userRepository: UserRepository,
                                  ratingsRepository: RatingRepository)
                                 (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) with LazyLogging {


  def ratingsForm() = Action.compose(authenticationAction).async { implicit request: MessagesRequest[AnyContent] => {
    val engine = recommendationEngineFactory.getEngine()
    val userName = request.session.get(AuthenticationAction.USER_SESSION_NAME).get
    userRepository
      .findByUserName(userName)
      .map {
        case Some(user) => engine
          .topPopularMovies(30)
          .map(list => list.map(movie => Rating(movie.title, "-1", movie.id)))
          .map(ratings => Ok(views.html.rate(ratingForm.fill(RatingListData(user.id, ratings)))))
        case None => Future.successful(BadRequest("User not found"))
      }.flatMap(identity)
  }
  }


  def ratings(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    ratingForm.bindFromRequest().fold(
      withError => Future(BadRequest("Form has errors")),
      ratingListData => {
        ratingsRepository
          .alterUserRatings(ratingListData.userId, ratingListData
            .ratings
            .filter(_.rating != "-1")
            .map(rating => models.dao.Rating(0, ratingListData.userId, rating.movieId, rating.rating.toDouble)))
        Future(Redirect(routes.RecommendationController.recommendation()))
      }
    )
  }
  }

  val ratingForm = Form(
    mapping(
      "userId" -> longNumber,
      "ratings" -> list[Rating](mapping(
        "title" -> text,
        "rating" -> text,
        "movieId" -> longNumber)
      (Rating.apply)(Rating.unapply))
    )(RatingListData.apply)(RatingListData.unapply)
  )
}

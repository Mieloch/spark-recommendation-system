package services.recommendation

import com.typesafe.scalalogging.LazyLogging
import models.Recommendation
import models.dao.{Movie, MovieRepository}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}

class RecommendationEngine(val ratings: RDD[Rating],
                           val model: MatrixFactorizationModel,
                           val sc: SparkContext,
                           val movieRepository: MovieRepository)(implicit ec: ExecutionContext)  {


  private val popularityByMovieDesc = ratings
    .map(rating => (rating.product, 1))
    .reduceByKey(_ + _)
    .filter(_._2 > 20)
    .sortBy(_._2, ascending = false)


  private val moviesByUser = ratings
    .map(rating => (rating.user, rating.product))
    .groupBy(_._1)
    .mapValues(_.map(_._2))
    .collect()
    .toMap

  def isRecommendationPossible(userId: Int): Boolean = {
    moviesByUser.contains(userId)
  }

  def top10RatingsFromAll(userId: Int): Future[List[Recommendation]] = {
    if (!isRecommendationPossible(userId)) {
      Future.successful(List.empty)
    } else {
      Logger.debug(s"Getting top 10 ratings from all movies for $userId")
      Future.sequence(
        model
          .recommendProducts(userId, 10)
          .map(toRecommendation)
          .toList
      )
    }
  }


  def top10RatingsFromMostPopular(userId: Int): Future[List[Recommendation]] = {
    if (!isRecommendationPossible(userId)) {
      Future.successful(List.empty)
    } else {
      Logger.debug(s"Getting top 10 ratings from most popular movies for $userId")
      val ratedMovies = moviesByUser(userId).toSet
      val topPopularNotRatedYet = popularityByMovieDesc
        .filter(movieAndPopularity => !ratedMovies.contains(movieAndPopularity._1))
        .take(100)
      val userAndMovie = topPopularNotRatedYet.map(movieAndPopularity => (userId, movieAndPopularity._1))
      Future.sequence(
        model
          .predict(sc.parallelize(userAndMovie))
          .sortBy(_.rating, ascending = false)
          .collect()
          .take(10)
          .map(toRecommendation)
          .toList
      )
    }
  }

  def topPopularMovies(n: Int) = {

    Future.sequence(popularityByMovieDesc
      .map(_._1)
      .take(n)
      .toList
      .map(id => Future.successful(id).zip(findMovieTitle(id)))
      .map(list => list.map(idWithTitle => Movie(idWithTitle._1, idWithTitle._2))))
  }


  private def toRecommendation(r: Rating) = {
    findMovieTitle(r.product).map(title => Recommendation(title, r.rating))
  }

  private def findMovieTitle(id: Long) = {
    movieRepository.findById(id)
      .map {
        case Some(movie) => movie.title
        case None => "unknown title"
      }
  }
}

package services.recommendation

import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}
import models.dao.MovieRepository
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import play.api.Configuration
import services.SparkConfig
import play.api.Logger
import scala.concurrent.ExecutionContext

@Singleton
class RecommendationEngineFactory @Inject()(a: Configuration,
                                            spark: SparkConfig,
                                            movieRepository: MovieRepository)
                                           (implicit ec: ExecutionContext) extends LazyLogging {

  private val rank: Int = 10
  private val numIteration: Int = 10

  private var engineInstance: RecommendationEngine = createNew()


  def getEngine(): RecommendationEngine = {
    if (engineInstance != null) engineInstance else createNew()
  }

  def createNew(): RecommendationEngine = {
    Logger.debug(s"Creating new model from with rank = $rank, numIteration = $numIteration")
    engineInstance = create(loadRatings())
    Logger.debug("New model created")
    engineInstance
  }

  private def create(ratings: RDD[Rating]): RecommendationEngine = {
    val model = ALS.train(ratings, rank, numIteration)
    new RecommendationEngine(ratings, model, spark.sparkSession.sparkContext, movieRepository)
  }


  private def loadRatings(): RDD[Rating] = {
    val url = a.get[String]("slick.dbs.default.db.url")
    val user = a.get[String]("slick.dbs.default.db.user")
    val pass = a.get[String]("slick.dbs.default.db.password")
    val connectionProperties = new Properties()
    connectionProperties.put("user", user)
    connectionProperties.put("password", pass)
    spark
      .sparkSession
      .read
      .jdbc(url, "recommendation_system.ratings", connectionProperties)
      .rdd
      .map { case Row(_, userId: Int, movieId: Int, rating: Double) => Rating(userId, movieId, rating) }
  }


}

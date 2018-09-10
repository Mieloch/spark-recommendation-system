package services

import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}
import org.apache.spark.sql.SparkSession
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@Singleton
class SparkConfig @Inject()(appLifecycle: ApplicationLifecycle) extends LazyLogging{
  System.setProperty("hadoop.home.dir", "C:\\Program Files\\winutils")
  val sparkSession: SparkSession = SparkSession
    .builder()
    .appName("RecommendationSystem")
    .master("local")
    .getOrCreate()
  appLifecycle.addStopHook { () =>
    logger.info("Closing spark session")
    sparkSession.close()
    Future.successful()
  }
}

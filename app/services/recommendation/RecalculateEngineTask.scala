package services.recommendation

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.Logger

@Singleton
class RecalculateEngineTask @Inject()(actorSystem: ActorSystem, recommendationEngineFactory: RecommendationEngineFactory)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.schedule(initialDelay = 1.minute, interval = 1.minute) {
    Logger.debug("Recalculating recommendation engine")
    recommendationEngineFactory.createNew()
    Logger.debug("Recommendation engine recalculated")
  }
}

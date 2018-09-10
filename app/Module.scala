import com.google.inject.AbstractModule
import services.recommendation.{RecalculateEngineTask, RecommendationEngineFactory}

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RecommendationEngineFactory]).asEagerSingleton()
    bind(classOf[RecalculateEngineTask]).asEagerSingleton()
  }
}

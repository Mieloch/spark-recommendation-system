package models.dao

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RatingRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class RatingsTable(tag: Tag) extends Table[Rating](tag, "ratings") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def movieId = column[Long]("movie_id")

    def rating = column[Double]("rating")


    def * = {
      (id, userId, movieId, rating) <> ((Rating.apply _).tupled, Rating.unapply)
    }
  }

  private val ratings = TableQuery[RatingsTable]

  def alterUserRatings(userId: Long, toInsert: List[Rating]): Unit = {
    val deletePrevoiusRatings = ratings.filter(_.userId === userId).delete
    val queries = toInsert.map(ratings.insertOrUpdate(_))
    db.run(DBIO.sequence(deletePrevoiusRatings :: queries).transactionally)
  }

  def findAllByUser(userId: Long): Future[Seq[Rating]] = db.run {
    ratings.filter(_.userId === userId).result
  }
}

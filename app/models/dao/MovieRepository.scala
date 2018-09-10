package models.dao

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovieRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class MoviesTable(tag: Tag) extends Table[Movie](tag, "movies") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def * = {
      (id, title) <> ((Movie.apply _).tupled, Movie.unapply)
    }
  }

  private val movies = TableQuery[MoviesTable]

  def findAll(): Future[Seq[Movie]] = db.run {
    movies.result
  }

  def findById(id: Long): Future[Option[Movie]] = {
    dbConfig.db.run(movies.filter(_.id === id).result.headOption)
  }
}

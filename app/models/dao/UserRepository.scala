package models.dao

import javax.inject.{Inject, Singleton}
import models.UserData
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class UsersTable(tag: Tag) extends Table[User](tag, "users") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def password = column[String]("password")

    def * = {
      (id, name, password) <> ((User.apply _).tupled, User.unapply)
    }
  }

  private val users = TableQuery[UsersTable]

  def findByUserNameAndPassword(name: String, password: String): Future[Option[User]] = {
    db.run(users.filter(_.name === name).filter(_.password === password).result.headOption)
  }

  def findByUserName(name: String): Future[Option[User]] = {
    db.run(users.filter(_.name === name).result.headOption)
  }

  def add(userData: UserData): Future[Int] = {
    db.run(users += User(0, userData.name, userData.password))
  }


}

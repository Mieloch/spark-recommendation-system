package models.dao

import play.api.libs.json.Json

case class User(id: Long, name: String, password: String) {

}
object User {
  implicit val userFormat = Json.format[User]
}
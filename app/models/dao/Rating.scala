package models.dao

import play.api.libs.json.Json

case class Rating(id: Long, userId: Long, movieId: Long, rating: Double) {

}

object Rating {
  implicit val movieFormat = Json.format[Rating]
}
package models.dao

import play.api.libs.json.Json


case class Movie(id: Long, title: String)

object Movie {
  implicit val movieFormat = Json.format[Movie]
}
package controllers

import javax.inject.Inject
import models.dao.MovieRepository
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

class MoviesController @Inject()(cc: ControllerComponents, repo: MovieRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def listAll() = Action.async { implicit request: Request[AnyContent] =>

    repo.findAll().map(movies => Ok(Json.toJson(movies)))
  }


}

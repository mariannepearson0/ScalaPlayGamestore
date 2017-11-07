package controllers

import javax.inject.Inject
import models.Game
import models.JsonFormats.gameFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo._
import reactivemongo.api.Cursor
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DBController @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  def collection: Future[JSONCollection] = database.map(
    _.collection[JSONCollection]("games"))

  def create = Action.async {
    val game = Game("F", "Gremlins Fight Back", 39.99, "Computer", "Most Popular")
    val futureResult = collection.flatMap(_.insert(game))
    futureResult.map(_ => Ok)
  }

//  def makeCollection: Future[List[Game]] = Action.async {
//    val cursor: Future[Cursor[Game]] = collection.map {
//      _.find(Json.obj()).sort(Json.obj("created" -> -1)).cursor[Game]
//    }
//    val futureGamesList: Future[List[Game]] = cursor.flatMap(_.collect[List]())
//    futureGamesList.map{games => Ok(games.toString)}
//    //Ok(views.html.homepageBS(futureGamesList)
//  }


  def findByName(searchID: String) = Action.async {
    val cursor: Future[Cursor[Game]] = collection.map {
      _.find(Json.obj("gameID" -> searchID)).
        sort(Json.obj("created" -> -1)).
        cursor[Game]
    }
    val futureUsersList: Future[List[Game]] = cursor.flatMap(_.collect[List]())
    futureUsersList.map { games =>
      Ok(games.toString)
    }
  }
}


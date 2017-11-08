package controllers

import javax.inject.Inject

import models.Game
import models.JsonFormats.gameFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DBController @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  var gamesList = scala.collection.mutable.ListBuffer[Game]()

  def collection: Future[JSONCollection] = database.map(
    _.collection[JSONCollection]("games"))

  def create = Action.async {
    val game = Game("F", "Gremlins Fight Back", 39.99, "The gremlins are back for another" +
        " skin crawling adventure. Raise Gizmo and help him defeat the evil Mogwai. A game which " +
        " Marianne's made up game magazine says is a MUST PLAY!", "images/gremplins.jpg")
    val futureResult = collection.flatMap(_.insert(game))
    futureResult.map(_ => Ok)
  }

//

  def homepage = Action.async {
    val cursor: Future[Cursor[Game]] = collection.map {
      _.find(Json.obj()).
        sort(Json.obj("created" -> -1)).
        cursor[Game]
    }
    val futureGamesList: Future[List[Game]] = cursor.flatMap(_.collect[List]())
    futureGamesList.map { games =>
      (for (game <- games) gamesList += game)
      Ok(views.html.homepageBS(gamesList))
    }
  }

  def removeAll = Action.async {
    val selector = BSONDocument()
    val futureRemove = collection.flatMap(_.remove(selector))
    futureRemove.map(_ => Ok)
  }

  def update: Action[AnyContent] = Action.async { implicit request =>
    val game = Game("A", "Sonic Forces", 25.99, "Sonic is fast, sonic " +
      "gets gold hoops,sonic has cool hair. Come be sonic on another rip roaring adventure. " +
      "We all want tobe a hedgehog at some point", raw"images/sonic.jpg")
    val selector = BSONDocument("title" -> "Sonic Forces")
    val futureResult = collection.map(_.findAndUpdate(selector,game))
    futureResult.map(_ => Ok("Updated user"))
  }
}


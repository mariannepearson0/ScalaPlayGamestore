package controllers

import javax.inject.Inject

import models.{Game, Payment, Suggestion}
import models.JsonFormats.gameFormat
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MainController @Inject() (val reactiveMongoApi: ReactiveMongoApi, val messagesApi:MessagesApi) extends Controller
  with MongoController with ReactiveMongoComponents with I18nSupport{

  var gamesList = scala.collection.mutable.Set[Game]()

  var basket = scala.collection.mutable.ArrayBuffer[String]()

  var sum:Double = 0

  def collection: Future[JSONCollection] = database.map(
    _.collection[JSONCollection]("games"))

//  def create = Action.async {
//    val game = Game("F", "Gremlins Fight Back", 39.99, "The gremlins are back for another" +
//        " skin crawling adventure. Raise Gizmo and help him defeat the evil Mogwai. A game which " +
//        " Marianne's made up game magazine says is a MUST PLAY!", "images/gremlins.jpg")
//    val futureResult = collection.flatMap(_.insert(game))
//    futureResult.map(_ => Ok)
//  }

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

  def gameInfoPage(id:String) = Action {
    Ok(views.html.gameInfoBSPage(id)(gamesList))
  }

  def contactUsPage = Action {
    Ok(views.html.contactUsBSPage(Suggestion.suggestions, Suggestion.createSuggestionForm))
  }

  def removeAll = Action.async {
    val selector = BSONDocument()
    val futureRemove = collection.flatMap(_.remove(selector))
    futureRemove.map(_ => Ok)
  }

  def update: Action[AnyContent] = Action.async { implicit request =>
    val game = Game("F", "Gremlins Fight Back", 36.99, "The gremlins are back for another " +
      "skin crawling adventure. Raise Gizmo and help him defeat the evil Mogwai. A game " +
      "which Marianne's made up game magazine calls a MUST PLAY!", raw"images/gremlins.jpg", "Most Popular")
    val selector = BSONDocument("gameID" -> "F")
    val futureResult = collection.map(_.findAndUpdate(selector,game))
    futureResult.map(_ => Ok("Updated user"))
  }

  def listSuggestions = Action { implicit request =>
    Ok(views.html.contactUsBSPage(Suggestion.suggestions, Suggestion.createSuggestionForm))
  }

  def listPayment = Action { implicit request =>

    Ok(views.html.checkoutPageBS(basket)(gamesList)(s"$sum")(Payment.payments, Payment.createPaymentForm))
  }

  def createSuggestion = Action { implicit request =>
    val formValidationResult = Suggestion.createSuggestionForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest(views.html.contactUsBSPage(Suggestion.suggestions, formWithErrors))
    }, { sug =>
      sug
      Suggestion.suggestions.append(sug)
      Redirect(routes.MainController.listSuggestions)
    })
  }

  def createPayment = Action { implicit request =>
    val formValidationResult = Payment.createPaymentForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest(views.html.checkoutPageBS(basket)(gamesList)(s"$sum")(Payment.payments, formWithErrors))
    }, { pay =>
      Payment.payments.append(pay)
      Redirect(routes.MainController.confirmOrder)
      //Redirect(routes.MainController.listPayment())
    })
  }

  def goToBasket = Action {
    Ok(views.html.checkoutPageBS(basket)(gamesList)(f"£$sum%2.2f")(Payment.payments, Payment.createPaymentForm))
  }

  def checkoutPage(gameId:String) = Action {
      basket += gameId
      for(game <- gamesList if game.gameID == gameId){
        sum += game.price
      }
      Ok(views.html.checkoutPageBS(basket)(gamesList)(f"£$sum%2.2f")(Payment.payments, Payment.createPaymentForm))
  }

//  def calculateTotal = Action {
//    //var newsum:Double = 0
//    for(item <- basket){
//      for(game <- gamesList if game.gameID == item){
//        sum += game.price
//      }
//    }
//    Ok(views.html.checkoutPageBS(basket)(gamesList)(sum))
//  }

  def removeItem = Action {
    if(basket.length>0) {
      for (game <- gamesList if game.gameID == basket(basket.length - 1)) {
        sum -= game.price
      }
      basket.remove(basket.length - 1)
    }
    Ok(views.html.checkoutPageBS(basket)(gamesList)(f"£$sum%2.2f")(Payment.payments, Payment.createPaymentForm))
  }

  def cancelOrder = Action {
    basket.clear()
    sum = 0
    Ok(views.html.homepageBS(gamesList))
  }

  def confirmOrder = Action {
    Ok(views.html.conformationPage(basket)(gamesList)(f"£$sum%2.2f")(Payment.payments))
  }

  def gameCats(category:String) = Action {
    Ok(views.html.gameCategories(gamesList)(category))
  }
}


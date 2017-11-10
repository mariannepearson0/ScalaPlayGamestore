package controllers

import javax.inject.Inject
import models.{Game, Payment, Suggestion, Suggest}
import models.JsonFormats.suggestionFormat
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
  var sugsList = scala.collection.mutable.Set[Suggest]()
  var basket = scala.collection.mutable.Map[String,Int]()
  var sum: Double = 0

  def gamesCollection: Future[JSONCollection] = database.map(
    _.collection[JSONCollection]("games"))

  def suggestionsCollection: Future[JSONCollection] = database.map(
    _.collection[JSONCollection]("suggestions")
  )

  def create = Action.async {
    val game = Game("I", "Monster Hunter", 2.99, "Set in the future when monstors came about " +
      "by accident. Kill monsters and use their armour as yours! Complex japanese rpg, if you " +
      "like a challenge this is the game for you.", "images/MonsterHunterWord.jpg", "New Releases", "https://www.youtube.com/embed/SE_FnuD9zJc")
    val futureResult = gamesCollection.flatMap(_.insert(game))
    futureResult.map(_ => Ok)
  }

  def homepage = Action.async {
      val cursor: Future[Cursor[Game]] = gamesCollection.map {
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
    val futureRemove = gamesCollection.flatMap(_.remove(selector))
    futureRemove.map(_ => Ok)
  }

  def update: Action[AnyContent] = Action.async { implicit request =>
    val game = Game("I", "Monster Hunter", 2.99, "Set in the future when monstors came about " +
      "by accident. Kill monsters and use their armour as yours! Complex japanese rpg, if you " +
      "like a challenge this is the game for you.", "images/MonsterHunterWorld.jpg", "New Releases", "https://www.youtube.com/embed/SE_FnuD9zJc")
    val selector = BSONDocument("title" -> "Monster Hunter")
    val futureResult = gamesCollection.map(_.findAndUpdate(selector,game))
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
      Suggestion.suggestions.append(sug)
      val suggy = Suggest(sug.name, sug.suggest)
      val futureResult = suggestionsCollection.flatMap(_.insert(suggy))
      futureResult.map(_=>Ok)
      Redirect(routes.MainController.listSuggestions)
    })
  }

  def allSuggestions = Action.async {
    val cursor: Future[Cursor[Suggest]] = suggestionsCollection.map {
      _.find(Json.obj()).
        sort(Json.obj("created" -> -1)).
        cursor[Suggest]
    }
    val futureSugList: Future[List[Suggest]] = cursor.flatMap(_.collect[List]())
    futureSugList.map { sugs =>
      (for (sug <- sugs) sugsList += sug)
      Ok(views.html.Suggestions(sugsList))
    }
  }

  def createPayment = Action { implicit request =>
    val formValidationResult = Payment.createPaymentForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest(views.html.checkoutPageBS(basket)(gamesList)(s"$sum")(Payment.payments, formWithErrors))
    }, { pay =>
      Payment.payments.append(pay)
      Redirect(routes.MainController.confirmOrder)
    })
  }

  def goToBasket = Action {
    Ok(views.html.checkoutPageBS(basket)(gamesList)(f"£$sum%2.2f")(Payment.payments, Payment.createPaymentForm))
  }

  def addToBasket(gameId:String) = Action {
    if(basket.contains(gameId)) {
      basket(gameId) = basket.get(gameId).get + 1
    }
    else basket(gameId) = 1
    for(game<-gamesList if game.gameID == gameId){
      sum += game.price
    }
    Redirect(routes.MainController.checkoutPage())
  }

  def checkoutPage = Action {
      Ok(views.html.checkoutPageBS(basket)(gamesList)(f"£$sum%2.2f")(Payment.payments, Payment.createPaymentForm))
  }

  def removeItem = Action {
    if(basket.size>0) {
      for (game <- gamesList if game.gameID == basket.keySet.last) {
        sum -= game.price
      }
      if(basket.get(basket.keySet.last).get > 1){
        basket(basket.keySet.last) = basket.get(basket.keySet.last).get - 1
      }
      else basket.remove(basket.keySet.last)
    }
    Ok(views.html.checkoutPageBS(basket)(gamesList)(f"£$sum%2.2f")(Payment.payments, Payment.createPaymentForm))
  }

  def cancelOrder = Action {
    basket.clear
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


package controllers

import play.api.mvc._

class Application extends Controller {



  def index = Action {
    Ok(views.html.index("Game Shop"))
  }

  def homepage = Action {
    Ok(views.html.homepageBS(List("Sonic Forces", "Call of Duty", "Singstar", "The Sims", "Fifa", "Gremlins Fight Back")))
  }

  def gameInfoPage = Action {
    Ok(views.html.gameInfoBSPage())
  }

  def checkoutPage = Action {
    Ok(views.html.Checkout())
  }

  def contactUsPage = Action {
    Ok(views.html.contactUsBSPage())
  }
}
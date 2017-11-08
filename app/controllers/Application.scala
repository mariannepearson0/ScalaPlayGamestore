package controllers

import models.Game
import play.api.mvc._

class Application extends Controller {



  def index = Action {
    Ok(views.html.index("Game Shop"))
  }


}
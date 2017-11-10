package models

import play.api.data._
import play.api.data.Forms._
import scala.collection.mutable.ArrayBuffer

case class Suggestion(name:String, suggest:String)

object Suggestion {

  val createSuggestionForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "suggest" -> nonEmptyText(minLength=10)
    )(Suggestion.apply)(Suggestion.unapply)
  )

  val suggestions = ArrayBuffer (
    Suggestion("Marianne", "More games on website"),
    Suggestion("James", "More shop locations"),
    Suggestion("Harry", "More shooter games")
  )
}

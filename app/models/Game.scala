package models

case class Game (
                gameID: String,
                title: String,
                price: Double,
                description:String,
                imageURL:String,
                category:String
                )

object JsonFormats {
  import play.api.libs.json.Json

  implicit val gameFormat = Json.format[Game]
}



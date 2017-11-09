package models

object JsonFormats {
  import play.api.libs.json.Json
  implicit val gameFormat = Json.format[Game]
  implicit val suggestionFormat = Json.format[Suggest]
}

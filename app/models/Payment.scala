package models

import play.api.data._
import play.api.data.Forms._
import scala.collection.mutable.ArrayBuffer

case class Payment(nameOnCard:String, cardNumber:String, Csc:Int, email:String)

object Payment {

  val createPaymentForm = Form(
    mapping(
      "nameOnCard" -> nonEmptyText.verifying(_.matches("^[a-z A-Z]*$")),
      "cardNumber" -> nonEmptyText(minLength=16, maxLength=16).verifying(_.matches("^[0-9]*$")),
      "Csc" -> number(min=100, max=999),
      "email" -> email
    )(Payment.apply)(Payment.unapply)
  )

  val payments = ArrayBuffer(
    Payment("Maria", "2342456473829463", 344, "mar@hotmail.com")
  )



}

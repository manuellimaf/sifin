package manuellimaf.sifin.model

trait Movement {

  val id: Option[Long]
  val amount: Double
  val currency: Currency
  val month: Month
}

case class Expense(id: Option[Long] = None, amount: Double, month: Month, day: Int, currency: Currency, paymentMethod: PaymentMethod,
                   category: Category, description: String, ref: Option[String] = None) extends Movement
case class Income(id: Option[Long] = None, own: Boolean, amount: Double, currency: Currency, month: Month, description: String, ref: Option[String] = None) extends Movement
case class Saving(id: Option[Long] = None, amount: Double, currency: Currency, month: Month, description: String, ref: Option[String]) extends Movement
case class Investment(id: Option[Long] = None, amount: Double, currency: Currency, month: Month, description: String, ref: Option[String]) extends Movement


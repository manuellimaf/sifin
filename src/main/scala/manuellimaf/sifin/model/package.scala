package manuellimaf.sifin

import java.time.YearMonth

package object model {
  case class Category(id: Long, name: String, description: String, isTax: Boolean)
  case class Month(var id: Option[Long] = None, name: String) {
    lazy val days = {
      val Seq(year, month) = name.split("/").toSeq
      YearMonth.of(year.toInt, month.toInt).lengthOfMonth
    }
  }
  case class Currency(id: Long, symbol: String, code: String)
  case class PaymentMethod(id: Long, name: String, methodType: String, description: String)
  case class Expense(id: Long, amount: Double, day: Int, currency: Currency, paymentMethod: PaymentMethod,
                     category: Category, ref: Option[String] = None)
  case class Income(id: Long, own: Boolean, amount: Double, currency: Currency, description: String, ref: Option[String] = None)

  case class IncomeData(own: Double, other: Double)
  case class ExpenseData(cash: Double, tc: Double, taxes: Double, total: Double, estimated: Double)
  case class MonthData(income: IncomeData,
                       expenses: ExpenseData,
                       savings: Double,
                       invested: Double,
                       available: Double,
                       usdPrice: Double)
  case class Serie(name: String, data: Seq[Double])
}

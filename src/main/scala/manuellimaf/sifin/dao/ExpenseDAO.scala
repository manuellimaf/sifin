package manuellimaf.sifin.dao

import manuellimaf.sifin.model._
import manuellimaf.sifin.service.DBConnectionSupport

object ExpenseDAO extends DBConnectionSupport {

  def getMonthExpenses(month: Month): Seq[Expense] =
    withQueryResult(
      """select e.id, e.amount, e.day, curr.id, curr.symbol, curr.code, pm.id, pm.name, pm.type, pm.description,
        | cat.id, cat.name, cat.description, cat.is_tax
        |from expense e
        |inner join currency curr on curr.id = e.currency_id
        |inner join payment_method pm on pm.id = e.payment_method_id
        |inner join category cat on cat.id = e.category_id
        |where e.month_id = ?""".stripMargin, Seq(month.id.get)) {
    res => Expense(id = res.getLong("e.id"),
      amount = res.getDouble("e.amount"),
      day = res.getInt("e.day"),
      currency = Currency(id = res.getLong("curr.id"),
        symbol = res.getString("curr.symbol"),
        code = res.getString("curr.code")),
      paymentMethod = PaymentMethod(id = res.getLong("pm.id"),
        name = res.getString("pm.name"),
        methodType = res.getString("pm.type"),
        description = res.getString("pm.description")),
      category = Category(id = res.getLong("cat.id"),
        name = res.getString("cat.name"),
        description = res.getString("cat.description"),
        isTax = res.getBoolean("cat.is_tax"))
    )
  }

  def getMonthExpensesByCategory(month: Month): Map[String, Double] =
    withQueryResult(
      """select c.description as cat, sum(e.amount) as amount
        |from expense e
        |inner join currency curr on curr.id = e.currency_id
        |inner join category c on c.id = e.category_id
        |where e.month_id = ?
        |group by c.description""".stripMargin, Seq(month.id.get)) {
      res => res.getString("cat") -> res.getDouble("amount")
    }.toMap


  def getMonthExpensesByDay(month: Month, currency: Currency): Map[Int, Double] =
    withQueryResult(
      """select e.day as day, sum(e.amount) as amount
        |from expense e
        |inner join currency curr on curr.id = e.currency_id
        |where e.month_id = ? and curr.id = ?
        |group by e.day""".stripMargin, Seq(month.id.get, currency.id)) {
      res => res.getInt("day") -> res.getDouble("amount")
    }.toMap

}


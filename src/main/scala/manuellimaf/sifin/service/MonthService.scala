package manuellimaf.sifin.service

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config
import manuellimaf.sifin.dao._
import manuellimaf.sifin.model._

trait MonthSupport {
  val monthService: MonthService = DefaultMonthService
}

trait MonthService {
  def getMonthMovements(monthId: Long): MonthData
}

object DefaultMonthService extends MonthService with Logging with Config {

  def getMonthMovements(monthId: Long): MonthData = {
    val month = CatalogDAO.getMonthById(monthId)
    val incomes = IncomeDAO.getMonthIncomes(month)
    val incomeData = toIncomeData(incomes)
    val expenses = ExpenseDAO.getMonthExpenses(month)
    val expensesData = toExpensesData(expenses, month)
    val savings = SavingsDAO.getMonthTotalSavings(month).getOrElse("$", 0d) // $ only for now
    val invested = SavingsDAO.getMonthTotalInvestments(month).getOrElse("$", 0d) // $ only for now
    val available = availability(incomeData, expensesData, savings, invested)
    val dollar = CatalogDAO.getCurrency("USD")
    val usdPrice = ExchangeRateDAO.getExchangeRate(month, dollar)
    MonthData(
      income = incomeData,
      expenses = expensesData,
      savings = savings,
      invested = invested,
      available = available,
      usdPrice = usdPrice.getOrElse(-1))
  }

  private def toExpensesData(expenses: Seq[Expense], month: Month): ExpenseData = {
    var cash = 0d
    var tc = 0d
    var taxes = 0d
    expenses.foreach {
      case Expense(_, amount, _, _, Currency(_, _, code), paymentMethod, category, _, _) if code == "ARS" =>
        if (category.categoryType == "TAX") {
          taxes += amount
        } else if (paymentMethod.methodType == "cash") {
          cash += amount
        } else {
          tc += amount
        }
      case _ => // Only $ for now
    }
    val monthDays = month.days
    val days = if(expenses.nonEmpty) expenses.map(_.day).max else monthDays
    val remainingDays = monthDays - days
    val estimated = cash / days * remainingDays
    ExpenseData(cash, tc, taxes, cash + tc + taxes, estimated = estimated)
  }

  private def toIncomeData(incomes: Seq[Income]): IncomeData = {
    var own = 0d
    var other = 0d
    incomes.foreach {
      case Income(_, isOwn, amount, Currency(_, _, code), _, _, _) if code == "ARS" =>
        if(isOwn) own += amount
        else other += amount
      case _ => // Only $ for now
    }
    IncomeData(own, other)
  }

  private def availability(incomes: IncomeData, expenses: ExpenseData, savings: Double, invested: Double): Double = {
    val income = incomes.own + incomes.other
    income - expenses.total - savings - invested
  }

}


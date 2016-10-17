package manuellimaf.sifin.service

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config
import manuellimaf.sifin.dao.{IncomeDAO, CatalogDAO, ExpenseDAO}
import manuellimaf.sifin.model._

trait MonthSupport {
  val monthService: MonthService = DefaultMonthService
}

trait MonthService {
  def getMonthMovements(monthId: Long): MonthData
}

object DefaultMonthService extends MonthService with DBConnectionSupport with Logging with Config {

  def getMonthMovements(monthId: Long): MonthData = {
    val month = CatalogDAO.getMonthById(monthId)
    val incomes = IncomeDAO.getMonthIncomes(month)
    val expenses = ExpenseDAO.getMonthExpenses(month)
    val savings = -1
    val invested = -1
    val available = -1
    val usdPrice = -1
    MonthData(
      income = toIncomeData(incomes),
      expenses = toExpensesData(expenses),
      savings = savings,
      invested = invested,
      available = available,
      usdPrice = usdPrice)
  }

  private def toExpensesData(expenses: Seq[Expense]): ExpenseData = {
    var cash = 0d
    var tc = 0d
    var taxes = 0d
    val estimated = -1d
    expenses.foreach {
      case Expense(_, amount, _, Currency(_, symbol), paymentMethod, category) if symbol == "$" =>
        if (category.isTax) {
          taxes += amount
        } else if (paymentMethod.methodType == "cash") {
          cash += amount
        } else {
          tc += amount
        }
      case _ => // Only $ for now
    }
    ExpenseData(cash, tc, taxes, cash + tc + taxes, estimated = estimated)
  }

  private def toIncomeData(incomes: Seq[Income]): IncomeData = {
    var own = 0d
    var other = 0d
    incomes.foreach {
      case Income(_, isOwn, amount, Currency(_, symbol), _) if symbol == "$" =>
        if(isOwn) own += amount
        else other += amount
      case _ => // Only $ for now
    }
    IncomeData(own, other)
  }
}


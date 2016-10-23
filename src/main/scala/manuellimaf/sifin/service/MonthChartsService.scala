package manuellimaf.sifin.service

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config
import manuellimaf.sifin.dao._
import manuellimaf.sifin.model.Serie

trait MonthChartsSupport {
  val monthChartsService: MonthChartsService = DefaultMonthChartsService
}

trait MonthChartsService {
  def getExpensesByCategory(monthId: Long): Seq[Serie]
  def getExpensesByDay(monthId: Long): Seq[Double]
}

object DefaultMonthChartsService extends MonthChartsService with Logging with Config {

  def getExpensesByCategory(monthId: Long): Seq[Serie] = {
    val month = CatalogDAO.getMonthById(monthId)
    val categories = CatalogDAO.getAllCategories.sortBy(_.description)
    val expenses = ExpenseDAO.getMonthExpensesByCategory(month)
    val data = categories.map(c => expenses.getOrElse(c.description, 0D))
    Seq(Serie("$", data))
  }

  def getExpensesByDay(monthId: Long): Seq[Double] = {
    val month = CatalogDAO.getMonthById(monthId)
    val currency = CatalogDAO.getCurrency("ARS") // Only $

    val expenses = ExpenseDAO.getMonthExpensesByDay(month, currency)
    (1 to month.days) map (day => expenses.getOrElse(day, 0D))
  }
}


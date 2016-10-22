package manuellimaf.sifin.controller

import manuellimaf.server.Controller
import manuellimaf.sifin.service.{MonthChartsSupport, MonthSupport}

trait MonthController extends MonthSupport with MonthChartsSupport {
  self: Controller =>

  get("/movements/:monthId") {
    val monthId = params("monthId").toLong
    asJson(monthService.getMonthMovements(monthId))
  }

  get("/expenses-by-category/:monthId") {
    val monthId = params("monthId").toLong
    asJson(monthChartsService.getExpensesByCategory(monthId))
  }

  get("/expenses-by-day/:monthId") {
    val monthId = params("monthId").toLong
    asJson(monthChartsService.getExpensesByDay(monthId))
  }
}

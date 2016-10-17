package manuellimaf.sifin.controller

import manuellimaf.server.Controller
import manuellimaf.sifin.service.MonthSupport

trait MonthController extends MonthSupport {
  self: Controller =>

  get("/movements/:monthId") {
    val monthId = params("monthId").toLong
    asJson(monthService.getMonthMovements(monthId))
  }

}

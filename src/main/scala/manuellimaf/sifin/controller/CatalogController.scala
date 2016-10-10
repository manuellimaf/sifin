package manuellimaf.sifin.controller

import manuellimaf.server.Controller
import manuellimaf.sifin.service.CatalogSupport

trait CatalogController extends CatalogSupport {
  self: Controller =>

  get("/categories") {
    asJson(catalogService.getAllCategories)
  }

  get("/months") {
    asJson(catalogService.getAllMonths)
  }

  delete("/user/:jira") {
  }

}

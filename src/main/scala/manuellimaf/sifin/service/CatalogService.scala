package manuellimaf.sifin.service

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config
import manuellimaf.sifin.dao.CatalogDAO
import manuellimaf.sifin.model.{Category, Month}

trait CatalogSupport {
  val catalogService: CatalogService = DefaultCatalogService
}

trait CatalogService {
  def getAllCategories: Seq[Category]
  def getAllMonths: Seq[Month]
}

object DefaultCatalogService extends CatalogService with DBConnectionSupport with Logging with Config {

  def getAllCategories: Seq[Category] = CatalogDAO.getAllCategories.sortBy(_.description)

  def getAllMonths: Seq[Month] = CatalogDAO.getAllMonths.sortBy(_.name)
}


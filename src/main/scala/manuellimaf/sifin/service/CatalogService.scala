package manuellimaf.sifin.service

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config
import manuellimaf.sifin.dao.CatalogDAO
import manuellimaf.sifin.model.{PaymentMethod, Currency, Category, Month}

trait CatalogSupport {
  val catalogService: CatalogService = DefaultCatalogService
}

trait CatalogService {
  def getPaymentMethod(name: String): PaymentMethod
  def getCurrency(code: String): Currency
  def getMonthOrNew(yearStr: String, monthStr: String): Month
  def getAllCategories: Seq[Category]
  def getAllMonths: Seq[Month]
  def getCategory(name: String): Category
}

object DefaultCatalogService extends CatalogService with DBConnectionSupport with Logging with Config {

  def getAllCategories: Seq[Category] = CatalogDAO.getAllCategories.sortBy(_.description)

  def getAllMonths: Seq[Month] = CatalogDAO.getAllMonths.sortBy(_.name)

  def getMonthOrNew(yearStr: String, monthStr: String): Month = {
    val name = s"$yearStr/$monthStr"
    CatalogDAO.findMonth(name).getOrElse {
      CatalogDAO.insertMonth(Month(name = name))
    }
  }

  def getCategory(name: String): Category = CatalogDAO.getCateogry(name)

  def getCurrency(code: String): Currency = CatalogDAO.getCurrency(code)

  def getPaymentMethod(name: String): PaymentMethod = CatalogDAO.getPaymentMethod(name)
}


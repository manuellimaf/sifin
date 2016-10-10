package manuellimaf.sifin.service

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config

trait CatalogSupport {
  val catalogService: CatalogService = DefaultCatalogService
}

trait CatalogService {
  def getAllCategories: Seq[Category]
  def getAllMonths: Seq[Month]
}

case class Category(id: Long, name: String)
case class Month(id: Long, name: String)

object DefaultCatalogService extends CatalogService with DBConnectionSupport with Logging with Config {

  def getAllCategories: Seq[Category] = withQueryResult("select id, name from category order by name") {
    res => Category(res.getLong("id"), res.getString("name"))
  }

  def getAllMonths: Seq[Month] = withQueryResult("select id, name from month order by name") {
    res => Month(res.getLong("id"), res.getString("name"))
  }

}


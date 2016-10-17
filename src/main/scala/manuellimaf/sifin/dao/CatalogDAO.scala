package manuellimaf.sifin.dao

import manuellimaf.sifin.model.{Month, Category}
import manuellimaf.sifin.service.DBConnectionSupport

object CatalogDAO extends DBConnectionSupport {

  def getAllCategories: Seq[Category] = withQueryResult("select id, name, description, is_tax from category order by name") {
    res => Category(
      id = res.getLong("id"),
      name = res.getString("name"),
      description = res.getString("description"),
      isTax = res.getBoolean("is_tax"))
  }

  def getAllMonths: Seq[Month] = withQueryResult("select id, name from month order by name") {
    res => Month(res.getLong("id"), res.getString("name"))
  }

  def getMonthById(id: Long) = withQueryResult("select id, name from month where id = ? limit 1", Seq(id)) {
    res => Month(res.getLong("id"), res.getString("name"))
  }.head

}


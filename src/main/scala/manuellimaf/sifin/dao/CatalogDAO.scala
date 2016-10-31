package manuellimaf.sifin.dao

import manuellimaf.sifin.model.{PaymentMethod, Category, Currency, Month}
import manuellimaf.sifin.service.DBConnectionSupport

object CatalogDAO extends DBConnectionSupport {

  def getPaymentMethod(name: String): PaymentMethod = withQueryResult("""select id, name, description, type from payment_method where name = ?""", Seq(name)) {
    res => PaymentMethod(id = res.getLong("id"), name = res.getString("name"), description = res.getString("description"), methodType = res.getString("type"))
  }.head


  def getCateogry(name: String): Category = withQueryResult("""select id, name, description, type from category where name = ?""", Seq(name)) {
    res => Category(
      id = res.getLong("id"),
      name = res.getString("name"),
      description = res.getString("description"),
      categoryType = res.getString("type"))
  }.head


  def insertMonth(month: Month): Month = {
    val inserted = executeUpdate("""insert into month (name) values (?)""".stripMargin, Seq(month.name))
    if(inserted == 1) {
      val id = withQueryResult("""select max(id) as id from month""")(_.getLong("id")).head
      month.id = Some(id)
      month
    } else {
      throw new Exception(s"Error inserting month (expected 1 record but $inserted inserted).")
    }
  }


  def findMonth(name: String): Option[Month] = withQueryResult("select id, name from month where name = ?", Seq(name)) {
    res => Month(Some(res.getLong("id")), res.getString("name"))
  }.headOption

  def getAllCategories: Seq[Category] = withQueryResult("select id, name, description, type from category order by name") {
    res => Category(
      id = res.getLong("id"),
      name = res.getString("name"),
      description = res.getString("description"),
      categoryType = res.getString("type"))
  }

  def getAllMonths: Seq[Month] = withQueryResult("select id, name from month order by name") {
    res => Month(Some(res.getLong("id")), res.getString("name"))
  }

  def getMonthById(id: Long) = withQueryResult("select id, name from month where id = ? limit 1", Seq(id)) {
    res => Month(Some(res.getLong("id")), res.getString("name"))
  }.head

  def getCurrency(code: String): Currency = withQueryResult("select id, symbol, code from currency where code = ? limit 1", Seq(code)) {
    res => Currency(res.getLong("id"), res.getString("symbol"), res.getString("code"))
  }.head

}


package manuellimaf.sifin.dao

import manuellimaf.sifin.model.Month
import manuellimaf.sifin.service.DBConnectionSupport

import scala.collection.mutable

object SavingsDAO extends DBConnectionSupport {

  def getMonthTotalSavings(month: Month): Map[String, Double] = {
    val savings = mutable.Map.empty[String, Double]
    withQueryResult(
      """select curr.symbol, sum(s.amount) as amount
        |from saving s
        |inner join currency curr on curr.id = s.currency_id
        |where s.month_id = ?
        |group by curr.symbol""".stripMargin, Seq(month.id)) {
      res => savings.put(res.getString("curr.symbol"), res.getDouble("amount"))
    }
    savings.toMap
  }

  def getMonthTotalInvestments(month: Month): Map[String, Double] = {
    val investments = mutable.Map.empty[String, Double]
    withQueryResult(
      """select curr.symbol, sum(i.amount) as amount
        |from investment i
        |inner join currency curr on curr.id = i.currency_id
        |where i.month_id = ?
        |group by curr.symbol""".stripMargin, Seq(month.id)) {
      res => investments.put(res.getString("curr.symbol"), res.getDouble("amount"))
    }
    investments.toMap
  }

}


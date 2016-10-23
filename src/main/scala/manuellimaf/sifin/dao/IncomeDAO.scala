package manuellimaf.sifin.dao

import manuellimaf.sifin.model._
import manuellimaf.sifin.service.DBConnectionSupport

object IncomeDAO extends DBConnectionSupport {

  def getMonthIncomes(month: Month): Seq[Income] =
    withQueryResult(
      """select i.id, i.own, i.amount, i.description, curr.id, curr.symbol, curr.code
        |from income i
        |inner join currency curr on curr.id = i.currency_id
        |where i.month_id = ?""".stripMargin, Seq(month.id.get)) {
      res => Income(id = res.getLong("i.id"),
        own = res.getBoolean("i.own"),
        amount = res.getDouble("i.amount"),
        currency = Currency(id = res.getLong("curr.id"),
          symbol = res.getString("curr.symbol"),
          code = res.getString("curr.code")),
        description = res.getString("i.description")
      )
    }

}


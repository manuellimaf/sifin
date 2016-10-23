package manuellimaf.sifin.dao

import manuellimaf.sifin.model.{Currency, Month}
import manuellimaf.sifin.service.DBConnectionSupport

object ExchangeRateDAO extends DBConnectionSupport {

  def getExchangeRate(month: Month, currency: Currency) = withQueryResult("select amount from exchange_rate where month_id = ? and currency_id = ? limit 1",
    Seq(month.id.get, currency.id)) {
    res => res.getDouble("amount")
  }.headOption

}


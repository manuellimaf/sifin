package manuellimaf.sifin

import java.io.InputStream
import java.nio.file.{Files, Paths}
import java.text.NumberFormat
import java.util.Locale

import manuellimaf.server.util.Logging
import manuellimaf.sifin.model.Month
import manuellimaf.sifin.service.CatalogSupport

import scala.io.Source

object FileImport {

  def main(args: Array[String]) = {

    import ExpenseImport._

    // leer un archivo
    val dataCsv =  Files.newInputStream(Paths.get("/home/manuel/Desktop/downloads/DataWin.csv"))

    // TODO - Parsear el archivo
    val movements = parseFile(dataCsv)

    // TODO - Importar todo (si no existe el mismo MD5) y persistirlo de manera transaccional

    // TODO - Actualizar el registro de importación. Estado: finalizado.
  }

}

object ExpenseImport extends CatalogSupport with Logging {

  def parseFile(dataCsv: InputStream): Seq[Any] = {
    val lineIterator = Source.fromInputStream(dataCsv, "utf-16le").getLines()
    lineIterator.next() // Skip headers line
    val movements = lineIterator map parseLine
    movements.toSeq
  }

  private val numberFormat = NumberFormat.getInstance(new Locale("es", "AR"))

  private def parseLine(line: String): Any = {
    line.split("\t").toSeq match {
      case Seq(_, _, dateStr, _, _, amountStr, _, _, _,
        notes, id, _, catCode, paymentCode, currCode, _, _) =>
        val (day, month) = parseDate(dateStr.trim)
        val amount = numberFormat.parse(amountStr.replaceAll("\"", ""))
        val category = catalogService.getCategory(catCode)
        val currency = catalogService.getCurrency(currCode)
        val payment = catalogService.getPaymentMethod(paymentCode)
        log.debug(s"day: $day | month: $month | amount: $amount | category: $category | currency: $currency | payment: $payment")
      case l => log.error(s"Unparseable line: $l")
    }
  }

  def parseDate(dateStr: String): (Int, Month) = {
    val (monthStr, dayStr) = dateStr.splitAt(6)
    val (yearStr, mStr) = monthStr.splitAt(4)
    val month = catalogService.getMonthOrNew(yearStr, mStr)
    val day = dayStr.toInt
    (day, month)
  }
}

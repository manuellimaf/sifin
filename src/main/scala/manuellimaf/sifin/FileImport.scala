package manuellimaf.sifin

import java.io.InputStream
import java.nio.file.{Files, Paths}
import java.text.NumberFormat
import java.util.Locale

import manuellimaf.server.util.Logging
import manuellimaf.sifin.model._
import manuellimaf.sifin.service.CatalogSupport

import scala.annotation.tailrec
import scala.io.Source

object FileImport {

  def main(args: Array[String]) = {

    import ExpenseImport._

    // leer un archivo
    val dataCsv =  Files.newInputStream(Paths.get("/home/manuel/Desktop/downloads/DataWin (1).csv"))

    // TODO - Parsear el archivo
    val movements = parseFile(dataCsv)

    // TODO - Importar todo (si no existe el mismo MD5) y persistirlo de manera transaccional

    // TODO - Actualizar el registro de importación. Estado: finalizado.
  }

}

object ExpenseImport extends CatalogSupport with Logging {

  def parseFile(dataCsv: InputStream): Seq[Movement] = {
    val lineIterator = Source.fromInputStream(dataCsv, "utf-16le").getLines()
    lineIterator.next() // Skip headers line

    @tailrec
    def parseLines(lines: Iterator[String], parsedLines: Seq[Option[Movement]]): Seq[Option[Movement]] = {
      if(lines.hasNext) parseLines(lineIterator, parsedLines :+ parseLine(lineIterator.next()))
      else parsedLines
    }

    parseLines(lineIterator, Seq.empty).flatten
  }

  private val numberFormat = NumberFormat.getInstance(new Locale("es", "AR"))

  private def parseLine(line: String): Option[Movement] = {
    line.split("\t").toSeq match {
      case Seq(_, _, dateStr, _, _, amountStr, _, _, _,
        notes, id, _, catCode, paymentCode, currCode, _, _) =>
        val (day, month) = parseDate(dateStr.trim)
        val amount = numberFormat.parse(amountStr.replaceAll("\"", "")).doubleValue()
        val category = catalogService.getCategory(catCode)
        val currency = catalogService.getCurrency(currCode)
        val payment = catalogService.getPaymentMethod(paymentCode)
        val movement = category.categoryType match {
          case "EXPENSE" =>
            Some(Expense(amount = amount, day = day, month = month, currency = currency, paymentMethod = payment, category = category, description = notes, ref = Some(id)))
          case t if t == "INCOME" || t =="DEVOLUTION" =>
            Some(Income(own = t == "INCOME", amount = amount, currency = currency, month = month, description = notes, ref = Some(id)))
          case "SAVING" =>
            Some(Saving(amount = amount, currency = currency, month = month, description = notes, ref = Some(id)))
          case "INVESTMENT" =>
            Some(Investment(amount = amount, currency = currency, month = month, description = notes, ref = Some(id)))
          case c =>
            log.error(s"Unrecognized category type: $c")
            None
        }
        log.debug(s"$movement")
        movement
      case l =>
        log.error(s"Unparseable line: $l")
        None
    }
  }

  private def parseDate(dateStr: String): (Int, Month) = {
    val (monthStr, dayStr) = dateStr.splitAt(6)
    val (yearStr, mStr) = monthStr.splitAt(4)
    val month = catalogService.getMonthOrNew(yearStr, mStr)
    val day = dayStr.toInt
    (day, month)
  }
}

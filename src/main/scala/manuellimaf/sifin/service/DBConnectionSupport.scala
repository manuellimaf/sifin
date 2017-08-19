package manuellimaf.sifin.service

import java.sql.{PreparedStatement, Connection, DriverManager, ResultSet}

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config

trait DBConnectionSupport extends Config with Logging {

  private def withDBConnection[T](block: Connection => T) = {
    val connection: Connection = DriverManager.getConnection(s"jdbc:mysql://$dbHost:$dbPort/sifin?useSSL=false", dbUser, dbPass)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }

  protected def withQueryResult[T](query: String, params: Seq[Any] = Seq.empty)(block: ResultSet => T): Seq[T] =
    withStatement(query, params) { statement =>
      val resultSet = statement.executeQuery()
      try {
        var r = Seq.empty[T]
        while (resultSet.next()) {
          r = r :+ block(resultSet)
        }
        r
      } finally {
        try {
          resultSet.close()
        } catch {
          case e: Exception => //nothing to do...
        }
      }
  }

  protected def executeUpdate(query: String, params: Seq[Any] = Seq.empty): Int =
    withStatement(query, params)(_.executeUpdate())

  private def withStatement[T](query: String, params: Seq[Any] = Seq.empty)(block: PreparedStatement => T) =
    withDBConnection { connection =>
      val statement: PreparedStatement = connection.prepareStatement(query)

      def bindParams(parameters: Seq[Any], idx: Int = 1): Unit = parameters match {
        case Nil => // Nothing to bind
        case p :: ps =>
          log.debug(s"Setting parameter[$idx]: $p")
          statement.setObject(idx, p)
          bindParams(ps, idx + 1)
      }

      bindParams(params)

      try {
        block(statement)
      } finally {
        try {
          statement.close()
        } catch {
          case e: Exception => //nothing to do...
        }
      }
  }

}

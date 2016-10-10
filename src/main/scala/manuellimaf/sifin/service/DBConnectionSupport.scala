package manuellimaf.sifin.service

import java.sql.{PreparedStatement, Connection, DriverManager, ResultSet}

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config

trait DBConnectionSupport extends Config with Logging {

  private def withDBConnection[T](block: Connection => T) = {
    val connection: Connection = DriverManager.getConnection(s"jdbc:mysql://$dbHost:$dbPort/sifin", dbUser, dbPass)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }

  protected def withQueryResult[T](query: String, params: Seq[Any] = Seq.empty)(block: ResultSet => T): Seq[T] = withDBConnection {
    connection =>
      val statement: PreparedStatement = connection.prepareStatement(query)

      def setParams(parameters: Seq[Any], idx: Int = 0): Unit = {
        if (parameters.nonEmpty) {
          statement.setObject(idx, parameters.head)
          setParams(parameters.tail, idx + 1)
        }
      }

      setParams(params)
      val resultSet = statement.executeQuery()
      try {
        var r = Seq.empty[T]
        while (resultSet.next()) {
          r = r :+ block(resultSet)
        }
        r
      } finally {
        try {
          statement.close()
          resultSet.close()
        } catch {
          case e: Exception => //nothing to do...
        }
      }
  }
}

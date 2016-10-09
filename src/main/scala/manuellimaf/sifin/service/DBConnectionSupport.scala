package manuellimaf.sifin.service

import java.sql.{Connection, DriverManager, ResultSet}

trait DBConnectionSupport {
  protected def withDBConnection[T](user: User, host: String, port: Int)(block: Connection => T) = {
    val connection: Connection = DriverManager.getConnection(s"jdbc:mysql://$host:$port", user.user, user.pass)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }


  protected def withQueryResult[T](connection: Connection, query: String)(block: ResultSet => T) = {
    val result = connection.prepareStatement(query).executeQuery()
    try {
      block(result)
    } finally {
      try {
        result.close()
      } catch {
        case e: Exception => //nothing to do...
      }
    }
  }
}

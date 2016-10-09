package manuellimaf.server.exception

case class BusinessException(message: String) extends RuntimeException(message)


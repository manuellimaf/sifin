package manuellimaf.sifin.config

import com.despegar.sbt.madonna.MadonnaConf

trait Config {
  import Config._

  val smtpHost = mail.getString("smtp.host")
  val smtpPort = mail.getInt("smtp.port")

  val dbHost = db.getString("host")
  val dbPort = db.getInt("port")
  val dbUser = db.getString("user")
  val dbPass = db.getString("pass")
}

object Config {
  private val config = MadonnaConf.config
  private val mail = config.getConfig("mail")
  private val db = config.getConfig("db")
}
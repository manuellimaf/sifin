package manuellimaf.sifin.config

import com.despegar.sbt.madonna.MadonnaConf

trait Config {
  import Config._

  val smtpHost = mail.getString("smtp.host")
  val smtpPort = mail.getInt("smtp.port")
}

object Config {
  private val config = MadonnaConf.config
  private val mail = config.getConfig("mail")
  private val db = config.getConfig("db")
}
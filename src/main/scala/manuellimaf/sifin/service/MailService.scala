package manuellimaf.sifin.service

import java.util.Properties
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config

object MailService extends Config with Logging {

  def send(subject: String, body: String, from: String,  to: Seq[String], cc: Seq[String], bcc: Seq[String], replyTo: String): Unit = {
    log.info("sending email...")
    try {
      val props = new Properties()
      props.put("mail.smtp.host", smtpHost)
      props.put("mail.smtp.port", smtpPort.toString)

      val message = new MimeMessage(Session.getInstance(props))

      message.setReplyTo(Array(new InternetAddress(replyTo, "")))
      message.setFrom(new InternetAddress(from, ""))
      to.foreach(e => message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(e).asInstanceOf[Array[Address]]))
      cc.foreach(e => message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(e).asInstanceOf[Array[Address]]))
      bcc.foreach(e => message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(e).asInstanceOf[Array[Address]]))

      message.setSubject(subject)
      message.setContent(body, "text/html; charset=utf-8")
      Transport.send(message)
      log.info("email sent successfully")
    } catch {
      case e: SendFailedException =>
        log.error(s"Fail to send email", e)
    }
  }

}

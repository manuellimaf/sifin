package manuellimaf.server

import org.eclipse.jetty.http.HttpVersion
import org.eclipse.jetty.server._
import org.eclipse.jetty.util.ssl.SslContextFactory

trait Https {
  self: MyServer =>

  override def configureHttps(server: Server) = {
    val http_config = new HttpConfiguration()
    http_config.setSecureScheme("https")
    http_config.setSecurePort(9443)
    http_config.setSendServerVersion(false)
    http_config.setSendDateHeader(false)

    val sslContextFactory = new SslContextFactory()

    sslContextFactory.setKeyStorePath(s"${sys.env("HOME")}/keys/private-key.jks")
    sslContextFactory.setKeyStorePassword("despegar")

    val https_config = new HttpConfiguration(http_config)
    https_config.addCustomizer(new SecureRequestCustomizer())

    val https = new ServerConnector(server,
      new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
      new HttpConnectionFactory(https_config))
    https.setPort(9443)
    https.setIdleTimeout(connectionIdleTimeout_ms)

    server.addConnector(https)
  }

}

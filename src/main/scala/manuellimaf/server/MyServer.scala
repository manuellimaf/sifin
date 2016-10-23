package manuellimaf.server

import java.util.concurrent.{SynchronousQueue, ThreadPoolExecutor, TimeUnit}
import javax.servlet.{ServletContext, ServletContextEvent}

import com.despegar.sbt.madonna.MadonnaConf
import org.eclipse.jetty.server.{HttpConfiguration, HttpConnectionFactory, Server, ServerConnector}
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.util.thread.ExecutorThreadPool
import org.scalatra.LifeCycle
import org.scalatra.servlet.ScalatraListener

trait MyServer extends Controller {
  protected val serverConfig = MadonnaConf.config.getConfig("server")
  val maxThreads = serverConfig.getInt("max-threads")
  val minThreads = serverConfig.getInt("min-threads")
  val threadsIdleTimeout_sec = 10
  protected val connectionIdleTimeout_ms = 30000

  val webDir = "web"
  val staticDir = "static"

  def main(args: Array[String]) = {

    //TODO: rejection policy and uncaught exception handler as a last effort to handle exceptions
    val executor: ThreadPoolExecutor = new ThreadPoolExecutor(minThreads, maxThreads, threadsIdleTimeout_sec, TimeUnit.SECONDS,
      new SynchronousQueue[Runnable](), NamedThreadFactory("jetty"))

    val threadPool = new ExecutorThreadPool(executor)

    val server = new Server(threadPool)

    val connector = createConnector(server)

    server.addConnector(connector)

    configureHttps(server)

    executor.setCorePoolSize(connector.getAcceptors * 2 + connector.getSelectorManager.getSelectorCount * 2 + minThreads)
    executor.prestartAllCoreThreads()

    server.setHandler(createServletContext)

    server.start()
    server.join()
  }

  protected def createConnector(server: Server): ServerConnector = {
    val connector = new ServerConnector(server)
    connector.setPort(9290)
    connector.setIdleTimeout(connectionIdleTimeout_ms)
    configureHttp(connector)
    connector
  }

  def urlFromClassPath(path: String) = {
    val resource = getClass.getClassLoader.getResource(path).toExternalForm
    print(resource)
    resource

  }

  protected def configureHttps(server: Server) = {}

  def createServletContext: ServletContextHandler = {
    val webappDir: String = Option(this.getClass.getClassLoader.getResource("webapp"))
      .map(_.toExternalForm)
      .filter(_.contains("jar:file:")) // this is a hack to distinguish in-jar mode from "expanded"
      .getOrElse("src/main/webapp")

    val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY)
    context setContextPath serverConfig.getString("context-path")
    context.setResourceBase(webappDir)
    context.setClassLoader(Thread.currentThread().getContextClassLoader)
    context.addServlet(classOf[DefaultServlet], "/")
    context.addEventListener(new CustomScalatraListener(this))
    context
  }

  def configureHttp(connector: ServerConnector): Unit = {
    val httpConfiguration: HttpConfiguration = connector.getConnectionFactory(classOf[HttpConnectionFactory]).getHttpConfiguration
    httpConfiguration.setSendServerVersion(false)
    httpConfiguration.setSendDateHeader(false)
  }

}

class CustomScalatraListener(server: MyServer) extends ScalatraListener {

  private var servletContext: ServletContext = _

  override protected def configureServletContext(sce: ServletContextEvent): Unit = {
    servletContext = sce.getServletContext
  }

  override def configureCycleClass(classLoader: ClassLoader) = {
    CustomLifeCycle(server).init(servletContext)
  }
}

case class CustomLifeCycle(server: MyServer) extends LifeCycle {
  override def init(context: ServletContext) = {
    context mount(server, "/*")
  }
}

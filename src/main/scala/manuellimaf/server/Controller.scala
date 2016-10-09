package manuellimaf.server

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import manuellimaf.server.exception.BusinessException
import manuellimaf.server.json.Json
import manuellimaf.server.util.Logging
import org.eclipse.jetty.server.Response
import org.scalatra.{BadRequest, RenderPipeline, ScalatraFilter}

trait Controller extends ScalatraFilter with Json with Logging {

  case class Error(errorType: String, request: String, errorMessage: String = "", stackTrace: Seq[String] = Seq())

  protected def withBody(block: String => Any): Any = {
    request.body match {
      case "" => BadRequest("bad")
      case _  => block(request.body)
    }
  }

  protected def withJsonBody[T: Manifest](block: T => Any): Any = {
    withBody { body =>
      val bodyObj = try {
        fromJson(body)
      } catch {
        case e: Exception =>
          throw new RuntimeException("Bad request. Body could be malformed")
      }
      block(bodyObj)
    }
  }

  override def renderPipeline = ({

    case req: Response ⇒
      val v = asJson(req)
      contentType = "application/json"
      response.writer.write(v)

  }: RenderPipeline) orElse super.renderPipeline

  override def renderUncaughtException(e: Throwable)(implicit request: HttpServletRequest, response: HttpServletResponse): Unit = {
    contentType = "application/json"

    def respondError(code: Int, error: Error) = {
      response.getWriter.write(asJson(error))
      response.setStatus(code)
    }

    e match {
      case BusinessException(msg) =>
        log.info(s"User error: $msg")
        respondError(450, Error("User error", request.getRequestURI, msg))
      case e: Exception =>
        log.error("Uncaught exception: ", e)
        respondError(500, Error("Internal error", request.getRequestURI, e.getMessage))
    }
  }
}
package manuellimaf.server

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

case class NamedThreadFactory(name: String) extends ThreadFactory  {
  val threadNumber: AtomicInteger = new AtomicInteger(1)
  override def newThread(r: Runnable): Thread = {
    val thread = new Thread(r, s"$name-${threadNumber.getAndIncrement()}")
    thread setDaemon true
    thread
  }
}

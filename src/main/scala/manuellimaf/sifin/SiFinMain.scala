package manuellimaf.sifin

import manuellimaf.server.{Https, MyServer}
import manuellimaf.sifin.controller._

object SiFinMain extends MyServer with Https
  with CatalogController
  with MonthController
  with HealthCheck
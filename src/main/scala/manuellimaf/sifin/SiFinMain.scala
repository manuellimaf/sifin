package manuellimaf.sifin

import manuellimaf.server.{Https, MyServer}
import manuellimaf.sifin.controller.{HealthCheck, CatalogController}

object SiFinMain extends MyServer with Https
  with CatalogController with HealthCheck
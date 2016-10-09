package manuellimaf.sifin

import manuellimaf.server.{Https, MyServer}
import manuellimaf.sifin.controller.{HealthCheck, UserController}

object SiFinMain extends MyServer with Https
  with UserController with HealthCheck
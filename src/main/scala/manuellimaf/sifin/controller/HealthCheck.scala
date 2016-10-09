package manuellimaf.sifin.controller

import manuellimaf.server.Controller

trait HealthCheck { self: Controller =>
  get("/health-check") {
    "ok"
  }
}
package manuellimaf.sifin.controller

import manuellimaf.server.Controller
import manuellimaf.sifin.service.{User, UserSupport}

trait UserController extends UserSupport {
  self: Controller =>

  userService.startUserHousekeeper()

  get("/user/:jira") {
      val user = User("", "", 1L, "")
      asJson(user)
  }

  delete("/user/:jira") {
  }

}

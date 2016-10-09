package manuellimaf.sifin.service

import manuellimaf.server.util.Logging
import manuellimaf.sifin.config.Config

trait UserSupport {
  val userService: UserService = DefaultUserService
}

trait UserService {
  def startUserHousekeeper()
}

case class User(user: String, pass: String, ttl: Long, lease_id: String)

object DefaultUserService extends UserService with Logging with Config {

  def startUserHousekeeper() = {
  }

}


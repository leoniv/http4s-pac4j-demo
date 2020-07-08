package com.test

import cats.effect._
import org.http4s._
import org.http4s.server._
import org.http4s.server.blaze._
import org.http4s.implicits._
import scala.concurrent.duration._
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.core.config.Config
import scala.collection.JavaConverters._
import org.pac4j.http4s._

object Main extends IOApp {
  type AuthClient = String

  def localLogoutService(config: Config) = new LogoutService(
    config,
    Some("/?defaulturlafterlogout"),
    destroySession = true
  )

  def centralLogoutService(config: Config) = new LogoutService(
    config,
    defaultUrl =
      Some("http://localhost:8080/?defaulturlafterlogoutafteridp"),
    destroySession = true,
    logoutUrlPattern = Some("http://localhost:8080/.*"),
    localLogout = false,
    centralLogout = true
  )

  def protectedPagesFilter(config: Config): Option[AuthClient] => HttpMiddleware[IO] =
    clients =>
      SecurityFilterMiddleware.securityFilter(
        config,
        clients = clients
      )

  // Helper to retrieve authorized profile from request
  def getProfiles(config: Config): Request[IO] => List[CommonProfile] = request => {
    val context = Http4sWebContext(request, config)
    val manager = new ProfileManager[CommonProfile](context)
    manager.getAll(true).asScala.toList
  }

  object cacheSessionStoreApp {
    val config = new DemoConfigFactory().build()
    config.setSessionStore(new Http4sCacheSessionStore())
    val callbackService = new CallbackService(config)

    def mkHttpApp: HttpApp[IO] = Routes(
      getProfiles(config),
      callbackService,
      localLogoutService(config),
      centralLogoutService(config),
      protectedPagesFilter(config)
    ).orNotFound
  }

  // NOTE: store session in the cookie may be a bad idea because cookie has
  // limited size!
  object cookieSessionStoreApp {
    val config = new DemoConfigFactory().build()
    config.setSessionStore(Http4sCookieSessionStore)
    val callbackService = new CallbackService(config)

    val sessionConfig = SessionConfig(
      cookieName = "session",
      mkCookie = ResponseCookie(_, _, path = Some("/")),
      secret = "This is a secret",
      maxAge = 5.minutes
    )

    def mkHttpApp: HttpApp[IO] =
      Session.sessionManagement(sessionConfig){
        Routes(
          getProfiles(config),
          callbackService,
          localLogoutService(config),
          centralLogoutService(config),
          protectedPagesFilter(config)
        )
      }.orNotFound
  }

  def mkServer(httpApp: HttpApp[IO]): Resource[IO, Server[IO]] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .resource

  def run(arg: List[String]): IO[ExitCode] =
    mkServer(cacheSessionStoreApp.mkHttpApp).use(_ => IO.never).as(ExitCode.Success)
}

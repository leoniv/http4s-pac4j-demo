package com.test

import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.dsl.io._
import org.http4s.server.HttpMiddleware
import scalatags.Text.all._
import scalatags.Text
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.http4s._
import cats.data.OptionT
import org.http4s.headers.Location

object Routes {
  import ScalatagsInstances._

  def renderProfiles(
    profiles: List[CommonProfile]
  ): List[Text.TypedTag[String]] =
    profiles.map { profile =>
      p()(b("Profile: "), profile.toString, br())
    }

  def loginPage(profiles: List[CommonProfile]) = html(
    body(
      h1("index"),
      a(href := "/login/facebook")(
        s"Protected url by Facebook: /facebook"
      ),
      "use a real account",
      br(),
      a(href := "/login/saml2")(
        "Protected url by SAML2: /saml2"
      ),
      "use testpac4j at gmail.com / Pac4jtest",
      br(),
      a(href := "/login/oidc")(
        "Protected url by OpenID Connect: /oidc"
      ),
      "(use a real account)",
      br(),
      a(href := "/login/form")(
        "Protected url by form authentication: /form"
      ),
      "(use username same as password)",
      br(),
      p(),
      a(href := "/protected")("Protected page"),
      br(),
      p(),
      a(href := "/logout")("Local Logout"),
      br(),
      renderProfiles(profiles)
    )
  )

  def localLoginForm =
    form(
      action := "http://localhost:8080/callback?client_name=FormClient",
      method := "POST"
    )(
      input(`type` := "text", name := "username", value := "")(),
      p(),
      input(
        `type` := "password",
        name := "password",
        value := ""
      )(),
      p(),
      input(
        `type` := "submit",
        name := "submit",
        value := "Submit"
      )()
    )

  def protectedPage(profiles: List[CommonProfile]) =
    div()(
      h1()("Protected Page"),
      renderProfiles(profiles)
    )


  def apply(
    getProfiles: Request[IO] => List[CommonProfile],
    callbackService: CallbackService,
    localLogoutService: LogoutService,
    centralLogoutService: LogoutService,
    protectedRoutesFilter: Option[Main.AuthClient] => HttpMiddleware[IO]
  ) = {

    val root: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case req @ GET -> Root => Ok(loginPage(getProfiles(req)))
      case GET -> Root / "loginForm" => Ok(localLoginForm)
      case GET -> Root / "favicon.ico" => NotFound()
      case req @ GET -> Root / "callback" => callbackService.login(req)
      case req @ POST -> Root / "callback" => callbackService.login(req)
      case req @ GET -> Root / "logout" => localLogoutService.logout(req)
      case req @ GET -> Root / "centralLogout" => centralLogoutService.logout(req)
    }

    val protectedRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case req @ GET -> _ => Ok(protectedPage(getProfiles(req)))
    }

    def callbackLoginPageClient = HttpRoutes.of[IO]{
      case req @ GET -> _ => Found("", Location(uri"http://localhost:8080/callback?client_name=LoginPageClient"))
    }

    val loginRoutes: HttpRoutes[IO] = HttpRoutes {
      case req @ GET -> Root / "form" =>
        protectedRoutesFilter(Some("FormClient"))
          .apply(callbackLoginPageClient)(req)
      case req @ GET -> Root / "facebook" =>
        protectedRoutesFilter(Some("FacebookClient"))
          .apply(callbackLoginPageClient)(req)
      case req @ GET -> Root / "oidc" =>
        protectedRoutesFilter(Some("OidcClient"))
          .apply(callbackLoginPageClient)(req)
      case req @ GET -> Root / "saml2" =>
        protectedRoutesFilter(Some("SAML2Client"))
          .apply(callbackLoginPageClient)(req)
      case req @ _ => OptionT.liftF(Ok(loginPage(Nil)))
    }

    Router[IO](
      "/" -> root,
      "/login" -> loginRoutes,
      "/protected" -> protectedRoutesFilter(Some("loginPageClient"))(protectedRoutes)
    )
  }
}

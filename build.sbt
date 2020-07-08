inThisBuild(
  Seq(
    scalaVersion := "2.12.11", // Also supports 2.11.x
    version := "1.0.0-eldis-SNAPSHOT",
    scalacOptions ++= Seq(
      "-Ypartial-unification",
      "-language:implicitConversions",
      "-language:higherKinds"
    )
  )
)

val circeVersion = "0.13.0"
val http4sVersion = "0.21.6"
val pac4jVersion = "3.8.3"
val specs2Version = "4.10.0"
val logbackVersion = "1.2.3"

lazy val http4sPac4j =
  RootProject(
    uri("https://github.com/eldis/http4s-pac4j.git#1.0.0-eldis-SNAPSHOT")
  )

val Deps = Seq(
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.pac4j" % "pac4j-core" % pac4jVersion,
  "org.pac4j" % "pac4j-cas" % pac4jVersion,
  "org.pac4j" % "pac4j-http" % pac4jVersion,
  "org.pac4j" % "pac4j-jwt" % pac4jVersion,
  "org.pac4j" % "pac4j-oauth" % pac4jVersion,
  "org.pac4j" % "pac4j-oidc" % pac4jVersion,
  "org.pac4j" % "pac4j-openid" % pac4jVersion,
  "org.pac4j" % "pac4j-saml" % pac4jVersion,
  "com.lihaoyi" %% "scalatags" % "0.6.7",
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "org.http4s" %% "http4s-server" % http4sVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

lazy val http4sPac4jDemo = project
 .in(file("."))
 .settings(
   libraryDependencies ++= Deps,
    //It's require for getting of net.shibboleth.tool:xmlsectool
    resolvers += "opensaml Repository" at
      "https://build.shibboleth.net/nexus/content/repositories/releases"
  ).dependsOn(http4sPac4j)

<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-http4s.png" width="300" />
</p>

This `http4s-pac4j-demo` project is an Http4s application to test the [http4s-pac4j](https://github.com/pac4j/http4s-pac4j) security library with various authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...

## Start & test

Build the project and launch the Http4s app on [http://localhost:8080](http://localhost:8080):

    $cd http4s-pac4j-demo
    $sbt
    sbt:http4sPac4jDemo> reStart
    sbt:http4sPac4jDemo> reStop

To test, you can call a protected url by clicking on the "Protected url by **xxx**" link, which will start the authentication process with the **xxx** provider.

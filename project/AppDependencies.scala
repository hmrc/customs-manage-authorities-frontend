import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "logback-json-logger" % "5.1.0",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.16.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "1.26.0-play-28",
    "uk.gov.hmrc" %% "auth-client" % "5.7.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.56.0",
    "org.typelevel" %% "cats-core" % "2.3.0"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2",
    "org.pegdown" % "pegdown" % "1.6.0",
    "org.jsoup" % "jsoup" % "1.12.1",
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "org.mockito" % "mockito-all" % "1.10.19",
    "org.scalacheck" %% "scalacheck" % "1.14.1",
    "com.github.tomakehurst" % "wiremock-standalone" % "2.25.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}

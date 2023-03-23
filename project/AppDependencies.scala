import sbt._
import play.core.PlayVersion

object AppDependencies {

  private val bootstrapVersion = "5.24.0"

  val compile = Seq(
     play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "6.8.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.71.0",
    "org.typelevel" %% "cats-core" % "2.3.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "5.24.0" % Test,
    "org.scalatest" %% "scalatest" % "3.1.0" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.pegdown" % "pegdown" % "1.6.0" % Test,
    "org.jsoup" % "jsoup" % "1.12.1" % Test,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % Test,
    "org.mockito" % "mockito-all" % "1.10.19" % Test,
    "org.scalacheck" %% "scalacheck" % "1.14.1" % Test,
    "com.github.tomakehurst" % "wiremock-standalone" % "2.25.0" % Test
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}

import sbt._
import play.core.PlayVersion

object AppDependencies {

  private val bootstrapVersion = "7.22.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.21.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.3.0",
    "org.typelevel" %% "cats-core" % "2.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.0.8",
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0",
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0",
    "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
    "org.mockito" % "mockito-core" % "3.12.4",
    "org.pegdown" % "pegdown" % "1.6.0",
    "org.jsoup" % "jsoup" % "1.16.1",
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "org.mockito" % "mockito-all" % "1.10.19",
    "org.scalacheck" %% "scalacheck" % "1.17.0",
    "com.github.tomakehurst" % "wiremock-standalone" % "2.27.2",
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.3.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.21.0-play-28"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}

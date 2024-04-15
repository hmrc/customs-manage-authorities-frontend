import sbt.*
import play.core.PlayVersion

object AppDependencies {

  private val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.0.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "1.8.0",
    "org.typelevel" %% "cats-core" % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.16",
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1",
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0",
    "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
    "org.mockito" % "mockito-core" % "5.11.0",
    "org.pegdown" % "pegdown" % "1.6.0",
    "org.jsoup" % "jsoup" % "1.16.1",
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "org.mockito" % "mockito-all" % "1.10.19",
    "org.scalacheck" %% "scalacheck" % "1.17.0",
    "com.github.tomakehurst" % "wiremock-standalone" % "2.27.2",
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "1.8.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}

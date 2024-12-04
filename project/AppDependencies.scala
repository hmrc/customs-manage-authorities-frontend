import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.5.0"
  private val mongoVersion = "2.3.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "11.6.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % mongoVersion,
    "uk.gov.hmrc" %% "play-partials-play-30" % "10.0.0",
    "org.typelevel" %% "cats-core" % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0",
    "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
    "org.mockito" % "mockito-core" % "5.11.0",
    "org.jsoup" % "jsoup" % "1.17.2",
    "org.mockito" % "mockito-all" % "1.10.19",
    "org.scalacheck" %% "scalacheck" % "1.17.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % mongoVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}

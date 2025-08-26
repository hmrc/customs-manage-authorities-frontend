import sbt.*

object AppDependencies {

  val bootstrapVersion     = "10.1.0"
  private val mongoVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "12.8.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % mongoVersion,
    "uk.gov.hmrc"       %% "play-partials-play-30"                 % "10.1.0",
    "org.typelevel"     %% "cats-core"                             % "2.12.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatestplus" %% "mockito-3-4"            % "3.2.10.0",
    "org.scalatestplus" %% "scalacheck-1-15"        % "3.2.11.0",
    "org.mockito"        % "mockito-core"           % "5.14.2",
    "org.jsoup"          % "jsoup"                  % "1.18.1",
    "org.mockito"        % "mockito-all"            % "1.10.19",
    "org.scalacheck"    %% "scalacheck"             % "1.18.1",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"     % mongoVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}

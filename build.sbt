import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.targetJvm
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "customs-manage-authorities-frontend"

val silencerVersion = "1.17.13"
val bootstrap = "7.22.0"
val scala2_13_8 = "2.13.8"

val testDirectory = "test"
val scalaStyleConfigFile = "scalastyle-config.xml"
val testScalaStyleConfigFile = "test-scalastyle-config.xml"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13_8

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(DefaultBuildSettings.scalaSettings *)
  .settings(DefaultBuildSettings.defaultSettings() *)
  .settings(inConfig(Test)(testSettings) *)
  //.settings(majorVersion := 0)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    targetJvm := "jvm-11",
    name := appName,
    RoutesKeys.routesImport += "models._",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._"
    ),
    PlayKeys.playDefaultPort := 9000,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;" +
      ".*javascript.*;.*Routes.*;.*GuiceInjector;",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageMinimumBranchTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq("-feature"),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    update / evictionWarningOptions :=
      EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    resolvers += Resolver.jcenterRepo,
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "lib/govuk-frontend/govuk/all.js",
          "javascripts/jquery.min.js",
          "javascripts/app.js"
        ))
    ),
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(concat,uglify),
    uglify / includeFilter := GlobFilter("application.js")
  )
  .configs(IntegrationTest)
  .settings(scalastyleSettings)
  .settings(
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}",
    scalacOptions += "-P:silencer:globalFilters=possible missing interpolator: detected interpolated identifier `\\$date`",
    scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "40"),
    scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates",
      //"-Ywarn-macros:none",
    //"-Wdead-code"
    ),
    Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )

lazy val scalastyleSettings = Seq(
  scalastyleConfig := baseDirectory.value / scalaStyleConfigFile,
  (Test / scalastyleConfig) := baseDirectory.value/ testDirectory / testScalaStyleConfigFile
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test")
  .settings(DefaultBuildSettings.itSettings)
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrap % Test))

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf",
    "-Dlogger.resource=logback-test.xml"
  )
)

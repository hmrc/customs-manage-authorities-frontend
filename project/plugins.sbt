resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.24.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.6.0")

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.6")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0"
  exclude("org.scala-lang.modules", "scala-xml_2.12"))

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")

addSbtPlugin("io.github.irundaia" % "sbt-sassify" % "1.5.2")

addSbtPlugin("com.github.sbt" % "sbt-concat" % "1.0.0")

addSbtPlugin("com.github.sbt" % "sbt-uglify" % "2.0.0")

addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.1.0")

addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.16.2")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")


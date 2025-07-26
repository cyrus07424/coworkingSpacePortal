lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  //.enablePlugins(PlayNettyServer).disablePlugins(PlayPekkoHttpServer) // uncomment to use the Netty backend
  .settings(
    name := """coworking-space-portal""",
    version := "1.0-SNAPSHOT",
    crossScalaVersions := Seq("2.13.16", "3.3.5"),
    scalaVersion := crossScalaVersions.value.head,
    libraryDependencies ++= Seq(
      guice,
      jdbc,
      ws, // HTTP client for Slack webhooks
      // Password hashing
      "org.mindrot" % "jbcrypt" % "0.4",
      // Test Database
      "com.h2database" % "h2" % "2.3.232",
      // Testing libraries for dealing with CompletionStage...
      "org.assertj" % "assertj-core" % "3.26.3" % Test,
      "org.awaitility" % "awaitility" % "4.2.2" % Test,
      "org.mockito" % "mockito-core" % "5.8.0" % Test,
    ),
    javacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-parameters",
      "-Xlint:unchecked",
      "-Xlint:deprecation",
      "-Werror"
    ),
    (Test / javaOptions) += "-Dtestserver.port=19001",
    // Make verbose tests
    (Test / testOptions) := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
  )

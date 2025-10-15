// Versión de Scala y ajustes globales
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / organization := "com.example"
ThisBuild / version      := "0.1.0"

// -------- Root agregador (no publica jar) ----------
lazy val root = (project in file("."))
  .aggregate(common, catalogService, orderService, notificationService)
  .settings(
    name := "bookstore",
    publish / skip := true
  )

// -------- Dependencias comunes ----------
lazy val V = new {
  val zio           = "2.0.19"
  val zioHttp       = "3.0.0-RC2"
  val zioJson       = "0.6.2"
  val zioConfig     = "4.0.0"
  val zioLogging    = "2.1.14"
  val quill         = "4.7.0"
  val postgres      = "42.6.0"
  val hikari        = "5.0.1"
  val flyway        = "9.16.0"
  val logback       = "1.4.11"
  val testcontainers = "0.41.3"
}

lazy val commonDeps = Seq(
  // ZIO Core
  "dev.zio" %% "zio" % V.zio,
  "dev.zio" %% "zio-streams" % V.zio,
  "dev.zio" %% "zio-json" % V.zioJson,
  "dev.zio" %% "zio-logging" % V.zioLogging,
  "dev.zio" %% "zio-logging-slf4j" % V.zioLogging,
  "dev.zio" %% "zio-config" % V.zioConfig,
  "dev.zio" %% "zio-config-typesafe" % V.zioConfig,
  "dev.zio" %% "zio-config-magnolia" % V.zioConfig,

  // Logging
  "ch.qos.logback" % "logback-classic" % V.logback,

  // Tests
  "dev.zio" %% "zio-test" % V.zio % Test,
  "dev.zio" %% "zio-test-sbt" % V.zio % Test,
  "dev.zio" %% "zio-test-magnolia" % V.zio % Test
)

lazy val serviceDeps = commonDeps ++ Seq(
  // ZIO HTTP (reemplazo de Akka HTTP)
  "dev.zio" %% "zio-http" % V.zioHttp,

  // Database
  "org.postgresql" % "postgresql" % V.postgres,
  "com.zaxxer" % "HikariCP" % V.hikari,
  "org.flywaydb" % "flyway-core" % V.flyway,
  "io.getquill" %% "quill-jdbc-zio" % V.quill
)

lazy val itTestDeps = Seq(
  "com.dimafeng" %% "testcontainers-scala-scalatest" % V.testcontainers % Test,
  "com.dimafeng" %% "testcontainers-scala-postgresql" % V.testcontainers % Test
)

// --------- Módulo common ----------
lazy val common = (project in file("common"))
  .settings(
    name := "common",
    libraryDependencies ++= commonDeps
  )

// --------- Microservicios ----------
lazy val catalogService = (project in file("catalog-service"))
  .dependsOn(common)
  .settings(
    name := "catalog-service",
    libraryDependencies ++= serviceDeps ++ itTestDeps,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val orderService = (project in file("order-service"))
  .dependsOn(common)
  .settings(
    name := "order-service",
    libraryDependencies ++= serviceDeps ++ itTestDeps,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val notificationService = (project in file("notification-service"))
  .dependsOn(common)
  .settings(
    name := "notification-service",
    libraryDependencies ++= serviceDeps ++ itTestDeps,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

// --------- Repositorios opcionales ----------
resolvers ++= Seq(
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

// Opciones del compilador Scala 3
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-language:postfixOps"
)
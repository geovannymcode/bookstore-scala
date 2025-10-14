// Versión de Scala y ajustes globales
ThisBuild / scalaVersion := "2.13.10"
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
  val akka        = "2.8.0"
  val akkaHttp    = "10.5.0"
  val alpakkaKafka= "4.0.2"
  val circe       = "0.14.5"
  val akkaHttpCirce = "1.39.2"
  val logback     = "1.4.11"
  val postgres    = "42.6.0"
  val hikari      = "5.0.1"
  val flyway      = "9.16.0"
  val quill       = "4.6.0"
  val pureconfig  = "0.17.4"
  val scalatest   = "3.2.15"
  val testcontainers = "0.41.3" // testcontainers-scala
}

lazy val commonDeps = Seq(
  // JSON
  "io.circe" %% "circe-core"    % V.circe,
  "io.circe" %% "circe-generic" % V.circe,
  "io.circe" %% "circe-parser"  % V.circe,
  // Logging
  "ch.qos.logback" %  "logback-classic" % V.logback,
  "com.typesafe"   %  "config"          % "1.4.2",
  "com.github.pureconfig" %% "pureconfig" % V.pureconfig,
  // Tests
  "org.scalatest" %% "scalatest" % V.scalatest % Test
)

lazy val serviceDeps = commonDeps ++ Seq(
  // Akka stack
  "com.typesafe.akka" %% "akka-actor-typed" % V.akka,
  "com.typesafe.akka" %% "akka-stream"      % V.akka,
  "com.typesafe.akka" %% "akka-http"        % V.akkaHttp,
  // Akka HTTP + Circe integration ← AGREGADO
  "de.heikoseeberger" %% "akka-http-circe"  % V.akkaHttpCirce,
  // Kafka con Akka (Alpakka Kafka)
  "com.typesafe.akka" %% "akka-stream-kafka" % V.alpakkaKafka,
  // DB
  "org.postgresql" %  "postgresql" % V.postgres,
  "com.zaxxer"     %  "HikariCP"   % V.hikari,
  "org.flywaydb"   %  "flyway-core"% V.flyway,
  "io.getquill"   %%  "quill-jdbc" % V.quill
)

lazy val itTestDeps = Seq(
  "com.dimafeng" %% "testcontainers-scala-scalatest" % V.testcontainers % Test,
  "com.dimafeng" %% "testcontainers-scala-postgresql"% V.testcontainers % Test,
  "com.dimafeng" %% "testcontainers-scala-kafka"     % V.testcontainers % Test
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
    libraryDependencies ++= serviceDeps ++ itTestDeps
  )

lazy val orderService = (project in file("order-service"))
  .dependsOn(common)
  .settings(
    name := "order-service",
    libraryDependencies ++= serviceDeps ++ itTestDeps
  )

lazy val notificationService = (project in file("notification-service"))
  .dependsOn(common)
  .settings(
    name := "notification-service",
    libraryDependencies ++= serviceDeps ++ itTestDeps
  )


// --------- Repositorios opcionales ----------
resolvers ++= Seq(
  "Sonatype OSS Releases"  at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

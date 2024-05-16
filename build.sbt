ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.9"

lazy val root = (project in file("."))
  .settings(
    name := "akka-kafka"
  )
val AkkaVersion = "2.6.20"
val AkkaStreamAlpakka = "4.0.0"
val AkkaHttpVersion = "10.2.9"
val AkkaStreamKafka = "3.0.1"
val JacksonVersion = "2.11.4"
val LogbackVersion = "1.2.3"
val ScalaTest = "3.1.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % AkkaStreamAlpakka,
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % AkkaStreamAlpakka,
  "com.lightbend.akka" %% "akka-stream-alpakka-s3" % AkkaStreamAlpakka,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % AkkaStreamKafka,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "org.scalatest" %% "scalatest" % ScalaTest % Test,
)
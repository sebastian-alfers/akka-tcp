name := "akka-tcp-server"

version := "1.0"

val akkaVersion = "2.3.12"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",

  "org.scalatest" %% "scalatest" % "2.2.+" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "com.typesafe.akka" %% "akka-testkit"   % akkaVersion  % "test",
  "org.specs2" %% "specs2-core" % "2.4.14"  % "test"
)

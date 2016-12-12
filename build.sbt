import sbt.Keys._

organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

crossPaths := false

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "io.github.silvaren" % "quotepersistence" % "1.0"
  )
}

Revolver.settings

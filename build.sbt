name := "free-exercise"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-encoding", "UTF8",
  "-target:jvm-1.8"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11"
)

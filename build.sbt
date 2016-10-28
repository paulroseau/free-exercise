name := "free-exercise"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-encoding", "UTF8",
  "-target:jvm-1.8"
)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % "1.2.0-M1"
)

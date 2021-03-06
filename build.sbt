name := "free-exercise"

scalaVersion := "2.11.8"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.2")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-encoding", "UTF8",
  "-target:jvm-1.8"
)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "org.typelevel" %% "cats-free" % "0.8.0",
  "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % "1.2.0-M1"
)

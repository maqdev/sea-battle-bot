organization := "com.maqdev"

name := "sea-battle-bot"

version := "0.0.1"

resolvers ++= Seq(
  Resolver.sonatypeRepo("public")
)

libraryDependencies ++= Seq(
  "com.maqdev" %% "sea-battle" % "0.0.1",
  "eu.inn" %% "binders-core" % "0.6.57",
  "eu.inn" %% "binders-json" % "0.6.36",
  "com.typesafe" % "config" % "1.2.1",
  "org.slf4j" % "slf4j-api" % "1.7.12",

  "io.spray"            %%  "spray-can"     % "1.3.2",
  "io.spray"            %%  "spray-routing" % "1.3.2",
  "io.spray"            %%  "spray-client"  % "1.3.2",
  "io.spray"            %%  "spray-testkit" % "1.3.2"  % "test",

  "com.typesafe.akka"   %%  "akka-actor"    % "2.3.6",
  "com.typesafe.akka"   %%  "akka-testkit"  % "2.3.6"   % "test",

  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

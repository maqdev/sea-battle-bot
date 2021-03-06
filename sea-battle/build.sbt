organization := "com.maqdev"

name := "sea-battle"

version := "0.0.1"

resolvers ++= Seq(
  Resolver.sonatypeRepo("public")
)

libraryDependencies ++= Seq(
  "eu.inn" %% "binders-core" % "0.6.57",
  "eu.inn" %% "binders-json" % "0.6.36",
  "eu.inn" %% "binders-cassandra" % "0.6.13",
  "com.typesafe" % "config" % "1.2.1",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.google.guava" % "guava" % "18.0"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

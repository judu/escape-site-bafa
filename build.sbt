name := """escape-site-bafa"""
organization := "xyz.durillon"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
   jdbc,
   ws,
   guice,
   "org.playframework.anorm" %% "anorm" % "2.6.5",
   "org.postgresql" % "postgresql" % "42.2.9"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "xyz.durillon.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "xyz.durillon.binders._"

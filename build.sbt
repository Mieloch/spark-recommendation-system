name := """RecommendationSystem"""
organization := "pl.echomil"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.12"


dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.6"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.9.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
//mysql
libraryDependencies +=  "mysql" % "mysql-connector-java" % "5.1.34"

//spark
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.1"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.3.1"

//logging
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pl.echomil.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pl.echomil.binders._"

name := "cs441-HW3"

version := "1.0"

scalaVersion := "2.11.8"


resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.11"

libraryDependencies += "com.typesafe.akka" % "akka-http-core_2.11" % "3.0.0-RC1"

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"

libraryDependencies += "com.typesafe.akka" % "akka-http-xml-experimental_2.11" % "2.4.11"

libraryDependencies += "org.json4s" % "json4s-jackson_2.11" % "3.4.2"

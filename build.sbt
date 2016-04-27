

name          := "chatbot"
organization  := "com.harko"
version       := "0.0.1"
scalaVersion  := "2.11.7"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")


libraryDependencies += "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.4"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.3.0"

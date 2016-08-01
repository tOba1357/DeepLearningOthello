name := """server"""

version := "1.0"

scalaVersion := "2.11.6"

enablePlugins(JavaAppPackaging)

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.3"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"


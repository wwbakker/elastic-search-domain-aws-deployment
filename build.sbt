name := "elastic-search-domain-aws-deployment"
organization := "nl.wwbakker"

version := "1.0"

scalaVersion := "2.12.6"
awsSdkVersion := "1.11.345"

scalacOptions += "-Ypartial-unification"

val awsSdkVersion = settingKey[String]("The version of the AWS SDK used in this deployment script.")

resolvers ++= Seq(Resolver.jcenterRepo)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

libraryDependencies ++= Seq (
  "com.monsanto.arch" %% "cloud-formation-template-generator" % "3.9.1"
).map(_.force())
libraryDependencies += "com.amazonaws" % "aws-java-sdk-cloudformation" % awsSdkVersion.value
libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

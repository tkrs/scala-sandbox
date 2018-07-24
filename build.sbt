name := "scala-sandbox"
organization := "com.tkrs.github"
version := "1.0.0"
scalaVersion := "2.12.6"

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:_",
  "-Xfuture",
  "-Ydelambdafy:method",
) ++ warningOptions

Compile / console / scalacOptions --= warningOptions

lazy val warningOptions = Seq(
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:_",
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-core" % "0.22.0",
  "com.github.finagle" %% "finch-circe" % "0.22.0",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.typelevel" %% "cats-free" % "1.2.0",
  "co.fs2" %% "fs2-io" % "0.10.5",
  "io.catbird" %% "catbird-util" % "18.7.0",
  "io.monix" %% "monix" % "3.0.0-RC1",
  "com.google.guava" % "guava" % "22.0",
).map(_.withSources)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4",
  "org.scalacheck" %% "scalacheck" % "1.14.0",
).map(_.withSources % "test")

enablePlugins(JmhPlugin)
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")

// lazy val root = project.in(file(".")).dependsOn(agni)
// lazy val agni = ProjectRef.apply(uri("https://github.com/yanana/agni.git"), "free")


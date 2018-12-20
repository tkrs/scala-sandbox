name := "scala-sandbox"
organization := "com.tkrs.github"
version := "1.0.0"
scalaVersion := "2.12.8"

scalacOptions := defaultOptions ++ warningOptions

Compile / console / scalacOptions --= warningOptions

ThisBuild / run / fork := true

lazy val defaultOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-feature",
  "-language:_",
  "-Xfuture",
  "-Ydelambdafy:method"
)

lazy val warningOptions = Seq(
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:_"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finchx-core"    % "0.26.1",
  "com.github.finagle" %% "finchx-circe"   % "0.26.1",
  "com.chuusai"        %% "shapeless"      % "2.3.3",
  "org.typelevel"      %% "cats-free"      % "1.5.0",
  "co.fs2"             %% "fs2-io"         % "1.0.1",
  "io.catbird"         %% "catbird-util"   % "18.12.0",
  "io.catbird"         %% "catbird-effect" % "18.12.0",
  "io.trane"           %% "arrows-twitter" % "0.1.23",
  "io.monix"           %% "monix"          % "3.0.0-RC1",
  "com.google.guava"   % "guava"           % "22.0"
)

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"  % "3.0.5",
  "org.scalacheck" %% "scalacheck" % "1.14.0"
).map(_ % Test)

libraryDependencies := libraryDependencies.value.map(_.withSources)

enablePlugins(JmhPlugin)
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

// lazy val root = project.in(file(".")).dependsOn(agni)
// lazy val agni = ProjectRef.apply(uri("https://github.com/yanana/agni.git"), "free")

scalafmtOnCompile := true

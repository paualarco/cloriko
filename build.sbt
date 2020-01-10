import Dependencies._

enablePlugins(PlayScala)

PB.protocVersion := "-v3.10.0"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.cloriko",
      scalaVersion := "2.12.10",
      version      := Version.version
    )),
    name := "cloriko"
  ).aggregate(common, master, frontend, slave)

lazy val frontend = (project in file("frontend"))
  .settings(
    name := "cloriko-frontend",
    libraryDependencies ++= FrontendDependencies,
    version := "0.0.3"
  ).enablePlugins(PlayScala)

lazy val common = (project in file("common"))
  .settings(
    name := "cloriko-common",
    libraryDependencies ++= CommonDependencies,
    version := "0.0.2"
  )
  //.dependsOn(frontend % "compile->compile;test->test")


lazy val master = (project in file("master"))
  .settings(
    name := "cloriko-master",
    libraryDependencies ++= ProjectDependencies,
    version := "0.0.1"
  )
  .dependsOn(common, frontend)
  .enablePlugins(JavaAppPackaging, DockerPlugin)


lazy val slave = (project in file("slave"))
  .settings(
    name := "cloriko-slave",
    //libraryDependencies ++= ProjectDependencies,
    version := Version.version
  )
  .dependsOn(common, master)





import ReleaseTransformations.*

val sbt1version: String = "1.13.0"
val sbt2version: String = {
  val p = new java.util.Properties
  p.load(new java.io.FileInputStream("project/build.properties"))
  p.getProperty("sbt.version").trim
}
val Scala212 = scala_version_from_sbt_version.ScalaVersionFromSbtVersion(sbt1version)
val Scala3 = scala_version_from_sbt_version.ScalaVersionFromSbtVersion(sbt2version)

val sunMiscUnsafeLazyValDetectorRoot =
  rootProject.autoAggregate.settings(
    autoScalaLibrary := false,
    publish / skip := true
  )

startYear := Some(2026)

organization := "com.github.xuwei-k"

val repo = "xuwei-k/sun-misc-unsafe-lazy-val-detector"

homepage := Some(uri(s"https://github.com/${repo}"))

licenses := Seq(License.MIT)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommandAndRemaining("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

publishTo := (if (isSnapshot.value) None else localStaging.value)

pomExtra := (
  <developers>
    <developer>
      <id>xuwei-k</id>
      <name>Kenji Yoshida</name>
      <url>https://github.com/xuwei-k</url>
    </developer>
  </developers>
  <scm>
    <url>git@github.com:xuwei-k/sum-misc-unsafe-lazy-val-detector.git</url>
    <connection>scm:git:git@github.com:xuwei-k/sum-misc-unsafe-lazy-val-detector.git</connection>
  </scm>
)

Compile / doc / scalacOptions ++= {
  val hash = sys.process.Process("git rev-parse HEAD").lazyLines_!.head
  scalaBinaryVersion.value match {
    case "2.12" =>
      Seq(
        "-sourcepath",
        (LocalRootProject / baseDirectory).value.getAbsolutePath,
        "-doc-source-url",
        s"https://github.com/${repo}/tree/${hash}€{FILE_PATH}.scala"
      )
    case "3" =>
      Seq(
        s"-source-links:github://${repo}",
        "-revision",
        hash
      )
  }
}

val core = projectMatrix
  .in(file("core"))
  .settings(
    name := "sun-misc-unsafe-lazy-val-detector",
    scriptedBufferLog := false,
    addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.2.0"),
    libraryDependencies += "org.ow2.asm" % "asm" % "9.10.1",
    scriptedLaunchOpts ++= Seq[(String, String)](
      "plugin.version" -> version.value
    ).map { (k, v) =>
      s"-D${k}=${v}"
    },
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.12" =>
          Seq(
            "-release:8",
            "-Xsource:3",
            "-Xlint"
          )
        case "3" =>
          Seq(
            "-Wunused:all"
          )
      }
    },
    scalacOptions ++= Seq(
      "-deprecation",
    ),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          sbt1version
        case "3" =>
          sbt2version
      }
    },
  )
  .defaultAxes(VirtualAxis.jvm)
  .enablePlugins(SbtPlugin)
  .jvmPlatform(
    Seq(Scala212, Scala3)
  )

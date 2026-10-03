addSbtPlugin("com.github.xuwei-k" % "sun-misc-unsafe-lazy-val-detector" % sys.props("plugin.version"))

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

InputKey[Unit]("check") := {
  val actual = sunMiscUnsafeLazyValDetectAll.value
  sbtBinaryVersion.value match {
    case "2" =>
      assert(actual.size == 2, actual)
      val map = actual.map(x => (x.groupId, x.artifactId, x.version) -> x.classNames).toMap
      val expected = Map(
        ("org.scalameta", "scalafmt-dynamic-core_3", "3.11.4") -> Seq(
          ("org/scalafmt/dynamic/ScalafmtDynamic$.class", 6),
          ("org/scalafmt/dynamic/ScalafmtDynamic.class", 1),
          ("org/scalafmt/dynamic/ScalafmtReflect.class", 14),
          ("org/scalafmt/dynamic/ScalafmtReflectConfig.class", 21),
        ),
        ("org.scalameta", "scalafmt-sysops_3", "3.11.4") -> Seq(
          ("org/scalafmt/sysops/GitOpsImpl.class", 7),
          ("org/scalafmt/sysops/PlatformRunOps$.class", 7),
        ),
      )
      assert(map == expected, map)
    case "1.0" =>
      assert(actual.isEmpty, actual)
  }
}

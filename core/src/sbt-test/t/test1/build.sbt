val common = Def.settings(
  scalaVersion := "3.9.0"
)

@transient
val check = taskKey[Unit]("")
@transient
val check2 = taskKey[Unit]("")

val a1 = project.settings(
  common,
  libraryDependencies += "io.netty" % "netty-common" % "4.2.18.Final",
  libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0",
  libraryDependencies += "org.scalatest" %% "scalatest-core" % "3.2.20" % Test,
  check := {
    assert((Compile / sunMiscUnsafeLazyValDetect).value.map(_.classNames).toSet == Set(catsValue, nettyValue))
    assert((Runtime / sunMiscUnsafeLazyValDetect).value.map(_.classNames).toSet == Set(catsValue, nettyValue))
    assert(
      (Test / sunMiscUnsafeLazyValDetect).value.map(_.classNames).toSet == Set(
        nettyValue,
        catsValue,
        scalacticValue,
        scalatestCoreValue,
        scalaXmlValue
      )
    )
  }
)

val a2 = project.settings(
  common,
  libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0" % Runtime,
  check := {
    assert((Compile / sunMiscUnsafeLazyValDetect).value.isEmpty)
    assert((Runtime / sunMiscUnsafeLazyValDetect).value.map(_.classNames) == Seq(scoptValue))
    assert((Test / sunMiscUnsafeLazyValDetect).value.map(_.classNames) == Seq(scoptValue))
  },
)

val root = project
  .in(file("."))
  .aggregate(a1, a2)
  .settings(
    common,
    check2 := {
      val actual = sunMiscUnsafeLazyValDetectAll.value.map(x => (x.groupId, x.artifactId))
      assert(
        actual == Seq(
          ("com.github.scopt", "scopt_3"),
          ("org.scala-lang.modules", "scala-xml_3"),
          ("org.scalactic", "scalactic_3"),
          ("org.scalatest", "scalatest-core_3"),
          ("org.typelevel", "cats-core_3"),
        ),
        actual
      )
    },
    check := {
      val actual = sunMiscUnsafeLazyValDetectAll.value
      assert(actual.size == 6)
      val map = actual.map(x => (x.groupId, x.artifactId) -> x.classNames).toMap
      assert(
        map(("io.netty", "netty-common")) == nettyValue,
        actual
      )
      assert(
        map(("com.github.scopt", "scopt_3")) == scoptValue,
        actual
      )
      assert(
        map(("org.typelevel", "cats-core_3")) == catsValue,
        actual
      )
      assert(
        map(("org.scalatest", "scalatest-core_3")) == scalatestCoreValue,
        actual
      )
      assert(
        map(("org.scalactic", "scalactic_3")) == scalacticValue,
        actual
      )
      assert(
        map(("org.scala-lang.modules", "scala-xml_3")) == scalaXmlValue,
        actual
      )
    }
  )

val catsValue = Seq(
  ("cats/EvalInstances$$anon$10.class", 7),
  ("cats/EvalInstances0$$anon$11.class", 7),
  ("cats/Later.class", 7),
  ("cats/data/ContT$DeferCont.class", 7),
  ("cats/data/IorInstances$$anon$6.class", 7),
  ("cats/data/IorTInstances$$anon$5.class", 7),
  ("cats/data/IorTInstances$$anon$9.class", 7),
  ("cats/data/IorTInstances1$$anon$17.class", 7),
  ("cats/data/RepresentableStore.class", 14),
  ("cats/instances/EqInstances$$anon$3$Deferred.class", 7),
  ("cats/instances/EquivInstances$$anon$5$Deferred.class", 7),
  ("cats/instances/FunctionInstancesBinCompat0$$anon$1$Deferred.class", 7),
  ("cats/instances/FunctionInstancesBinCompat0$$anon$3$Deferred.class", 7),
  ("cats/instances/HashInstances$$anon$2$Deferred.class", 7),
  ("cats/instances/OrderInstances$$anon$3$Deferred.class", 7),
  ("cats/instances/OrderingInstances$$anon$3$Deferred.class", 7),
  ("cats/instances/PartialOrderInstances$$anon$3$Deferred.class", 7),
  ("cats/instances/PartialOrderingInstances$$anon$4$Deferred.class", 7),
  ("cats/instances/ShowInstances$$anon$1$Deferred.class", 7),
)

val scalatestCoreValue = Seq(
  ("org/scalatest/Assertions$.class", 5),
  ("org/scalatest/Assertions.class", 1),
  ("org/scalatest/AsyncSuperEngine.class", 11),
  ("org/scalatest/ConfigMapWrapperSuite.class", 11),
  ("org/scalatest/DeferredAbortedSuite.class", 6),
  ("org/scalatest/DispatchReporter.class", 6),
  ("org/scalatest/DocSpec.class", 16),
  ("org/scalatest/NonImplicitAssertions$.class", 5),
  ("org/scalatest/NonImplicitAssertions.class", 1),
  ("org/scalatest/PrivateMethodTester$.class", 5),
  ("org/scalatest/PrivateMethodTester.class", 1),
  ("org/scalatest/Resources$.class", 6),
  ("org/scalatest/Sequential.class", 6),
  ("org/scalatest/ShellImpl.class", 46),
  ("org/scalatest/Stepwise.class", 6),
  ("org/scalatest/Suites.class", 6),
  ("org/scalatest/SuperEngine.class", 11),
  ("org/scalatest/concurrent/AbstractPatienceConfiguration$.class", 5),
  ("org/scalatest/concurrent/AbstractPatienceConfiguration.class", 1),
  ("org/scalatest/concurrent/Conductors$Conductor.class", 16),
  ("org/scalatest/concurrent/Eventually$.class", 5),
  ("org/scalatest/concurrent/Eventually.class", 1),
  ("org/scalatest/concurrent/Futures$.class", 5),
  ("org/scalatest/concurrent/Futures.class", 1),
  ("org/scalatest/concurrent/ScalaFutures$.class", 5),
  ("org/scalatest/concurrent/ScalaFutures.class", 1),
  ("org/scalatest/concurrent/Waiters$.class", 10),
  ("org/scalatest/concurrent/Waiters.class", 1),
  ("org/scalatest/events/Event.class", 11),
  ("org/scalatest/exceptions/StackDepthException.class", 21),
  ("org/scalatest/package$.class", 46),
  ("org/scalatest/prop/Configuration$.class", 40),
  ("org/scalatest/prop/Configuration$Parameter.class", 6),
  ("org/scalatest/prop/Configuration.class", 1),
  ("org/scalatest/prop/TableDrivenPropertyChecks$.class", 5),
  ("org/scalatest/prop/TableDrivenPropertyChecks.class", 1),
  ("org/scalatest/prop/Tables$.class", 5),
  ("org/scalatest/prop/Tables.class", 1),
  ("org/scalatest/time/Span.class", 16),
  ("org/scalatest/tools/DashboardReporter$TestRecord.class", 6),
  ("org/scalatest/tools/DiscoverySuite.class", 6),
  ("org/scalatest/tools/DistributedTestRunnerSuite.class", 6),
  ("org/scalatest/tools/Framework$ScalaTestTask.class", 21),
  ("org/scalatest/tools/Framework$Skeleton$1.class", 11),
  ("org/scalatest/tools/IconEmbellishedListCellRenderer.class", 6),
  ("org/scalatest/tools/JUnitXmlReporter.class", 6),
  ("org/scalatest/tools/PrettyPrinter.class", 6),
  ("org/scalatest/tools/ScalaTestFramework.class", 6),
  ("org/scalatest/tools/SuiteParam.class", 6),
)

val scalacticValue = Seq(
  ("org/scalactic/AndBool.class", 11),
  ("org/scalactic/BinaryMacroBool.class", 11),
  ("org/scalactic/ExistsMacroBool.class", 6),
  ("org/scalactic/IsInstanceOfMacroBool.class", 6),
  ("org/scalactic/LengthSizeMacroBool.class", 6),
  ("org/scalactic/NotBool.class", 6),
  ("org/scalactic/OrBool.class", 11),
  ("org/scalactic/Resources$.class", 6),
  ("org/scalactic/SimpleBool.class", 6),
  ("org/scalactic/SimpleMacroBool.class", 6),
  ("org/scalactic/UnaryMacroBool.class", 6),
  ("org/scalactic/source/ObjectMeta$$anon$1.class", 11),
  ("org/scalactic/source/Position$.class", 5),
  ("org/scalactic/source/Position.class", 1),
)

val scalaXmlValue = Seq(
  ("scala/xml/PrettyPrinter.class", 6),
  ("scala/xml/XML$$anon$1.class", 6),
  ("scala/xml/XML$.class", 6),
  ("scala/xml/dtd/DFAContentModel.class", 6),
  ("scala/xml/dtd/impl/Base$Eps$.class", 6),
  ("scala/xml/dtd/impl/Base$Star.class", 6),
  ("scala/xml/dtd/impl/Base.class", 16),
  ("scala/xml/dtd/impl/WordExp$Letter.class", 6),
  ("scala/xml/dtd/impl/WordExp$Wildcard.class", 6),
  ("scala/xml/parsing/FactoryAdapter.class", 6),
)

val scoptValue = Seq(
  ("scopt/OParser$.class", 10),
  ("scopt/OParser.class", 1),
  ("scopt/OptionParser.class", 6)
)

val nettyValue = Seq(
  ("io/netty/util/internal/PlatformDependent0$2.class", 3),
  ("io/netty/util/internal/PlatformDependent0$3.class", 2),
  ("io/netty/util/internal/PlatformDependent0$5.class", 6),
  ("io/netty/util/internal/PlatformDependent0.class", 79),
  ("io/netty/util/internal/shaded/org/jctools/queues/BaseLinkedQueueConsumerNodeRef.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/queues/BaseLinkedQueueProducerNodeRef.class", 3),
  ("io/netty/util/internal/shaded/org/jctools/queues/BaseMpscLinkedArrayQueueColdProducerFields.class", 2),
  ("io/netty/util/internal/shaded/org/jctools/queues/BaseMpscLinkedArrayQueueConsumerFields.class", 2),
  ("io/netty/util/internal/shaded/org/jctools/queues/BaseMpscLinkedArrayQueueProducerFields.class", 2),
  ("io/netty/util/internal/shaded/org/jctools/queues/LinkedQueueNode.class", 2),
  ("io/netty/util/internal/shaded/org/jctools/queues/MpmcArrayQueueConsumerIndexField.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/queues/MpmcArrayQueueProducerIndexField.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/queues/MpscArrayQueueConsumerIndexField.class", 2),
  ("io/netty/util/internal/shaded/org/jctools/queues/MpscArrayQueueProducerIndexField.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/queues/MpscArrayQueueProducerLimitField.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/queues/unpadded/MpscUnpaddedArrayQueueConsumerIndexField.class", 2),
  ("io/netty/util/internal/shaded/org/jctools/queues/unpadded/MpscUnpaddedArrayQueueProducerIndexField.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/queues/unpadded/MpscUnpaddedArrayQueueProducerLimitField.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/util/UnsafeAccess.class", 1),
  ("io/netty/util/internal/shaded/org/jctools/util/UnsafeLongArrayAccess.class", 6),
  ("io/netty/util/internal/shaded/org/jctools/util/UnsafeRefArrayAccess.class", 6),
)

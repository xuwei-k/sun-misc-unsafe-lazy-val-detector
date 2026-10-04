# Detect Scala 3 old `sun.misc.Unsafe` lazy val in sbt project classpath

[![Maven Central](https://img.shields.io/maven-central/v/com.github.xuwei-k/sun-misc-unsafe-lazy-val-detector_sbt2_3?label=Maven%20Central)](https://central.sonatype.com/artifact/com.github.xuwei-k/sun-misc-unsafe-lazy-val-detector_sbt2_3)

`project/plugins.sbt`
 
```scala
addSbtPlugin("com.github.xuwei-k" % "sun-misc-unsafe-lazy-val-detector" % "version")
```

sbt shell

```
> sunMiscUnsafeLazyValDetectAllPrint
```

# Detect Scala 3 old `sun.misc.Unsafe` lazy val in sbt project classpath

`project/plugins.sbt`
 
```scala
addSbtPlugin("com.github.xuwei-k" % "sun-misc-unsafe-lazy-val-detector" % "version")
```

sbt shell

```
> sunMiscUnsafeLazyValDetectAllPrint
```

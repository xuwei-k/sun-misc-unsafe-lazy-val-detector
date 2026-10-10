package sun_misc_unsafe_lazy_val_detector

import java.io.FileInputStream
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM9
import sbt.*
import sbt.Def
import sbt.Keys.*
import sbt.plugins.JvmPlugin
import sbt.util.CacheImplicits.{*, given}
import sbtcompat.PluginCompat
import scala.util.Using
import sjsonnew.Builder
import sjsonnew.JsonFormat
import sjsonnew.Unbuilder

object SunMiscUnsafeLazyValDetector extends AutoPlugin with SunMiscUnsafeLazyValDetectorCompat {
  object autoImport {
    val sunMiscUnsafeLazyValDetect = taskKey[Seq[SunMiscUnsafeLazyValValue]]("")
    val sunMiscUnsafeLazyValDetectAll = taskKey[Seq[SunMiscUnsafeLazyValValue]]("")
    val sunMiscUnsafeLazyValDetectAllExternalDependencies = taskKey[Seq[Classpath]]("").withRank(KeyRanks.Invisible)
    @transient
    val sunMiscUnsafeLazyValDetectAllPrint = taskKey[Unit]("")
    @transient
    val sunMiscUnsafeLazyValDetectClearCache = taskKey[Unit]("")
    val sunMiscUnsafeLazyValDetectDirectUnsafe = settingKey[Boolean]("")
    @transient
    val sunMiscUnsafeLazyValDetectPrint = taskKey[Unit]("")
  }

  import autoImport.*

  override def requires: Plugins = JvmPlugin

  override def trigger: PluginTrigger = allRequirements

  private val allConfig: Seq[Configuration] = Seq(Compile, Test, Runtime)

  private val defaultLogLevel = Level.Info

  private implicit val logLevelInstance: JsonFormat[Level.Value] = {
    val int = implicitly[JsonFormat[Int]]
    new JsonFormat[Level.Value] {
      override def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Level.Value = {
        val n = int.read(jsOpt, unbuilder)
        Level.values.find(_.id == n).getOrElse(defaultLogLevel)
      }

      override def write[J](obj: Level.Value, builder: Builder[J]): Unit =
        int.write(obj.id, builder)
    }
  }

  override val buildSettings: Seq[Def.Setting[?]] = Def.settings(
    sunMiscUnsafeLazyValDetectClearCache := {
      val size = cache.cache.size
      streams.value.log.info(s"clear ${size} cache")
      cache.cache.clear()
    },
    sunMiscUnsafeLazyValDetectDirectUnsafe := true,
    sunMiscUnsafeLazyValDetectAllExternalDependencies := Def.taskDyn {
      buildStructure.value.allProjectPairs
        .filter(_._1.autoPlugins.contains(SunMiscUnsafeLazyValDetector))
        .flatMap { case (_, x) =>
          allConfig.map { c =>
            x / c / externalDependencyClasspath
          }
        }
        .join
    }.value,
    sunMiscUnsafeLazyValDetectAll / logLevel := defaultLogLevel,
    sunMiscUnsafeLazyValDetectAll := {
      implicit val converter: xsbti.FileConverter = fileConverter.value
      sunMiscUnsafeLazyValDetectAllExternalDependencies.value.flatten.distinct.flatMap { lib =>
        val path = PluginCompat.toFile(lib)
        lib
          .get(PluginCompat.moduleIDStr)
          .map(PluginCompat.parseModuleIDStrAttribute)
          .map(moduleId => (moduleId, path, lib.data))
          .orElse {
            println(s"not found moduleId ${path}")
            None
          }
      }.groupBy(_._1)
        .map { case (_, v) => v.head }
        .toSeq
        .flatMap { case (moduleId, path, lib) =>
          if (!path.isFile) {
            println(s"${path} is not file. ${moduleId}")
            Nil
          } else if ((moduleId.organization == scalaOrganization.value) && (moduleId.name == "scala-library")) {
            Nil
          } else {
            val log = (sunMiscUnsafeLazyValDetectAll / logLevel).value
            val directUnsafe = sunMiscUnsafeLazyValDetectDirectUnsafe.value
            val lazyVals = getOrElseUpdateCache(
              SunMiscUnsafeLazyValDetectorCache.Key(lib, directUnsafe),
              () => {
                if (log.id <= Level.Info.id) {
                  println(s"[SunMiscUnsafeLazyValDetector] parse ${path}")
                }
                oldLazyValAndUnsafe(path, directUnsafe)
              }
            )
            if (lazyVals.nonEmpty) {
              Seq(
                SunMiscUnsafeLazyValValue(
                  moduleId.organization,
                  moduleId.name,
                  moduleId.revision,
                  lib,
                  lazyVals,
                )
              )
            } else {
              Nil
            }
          }
        }
        .sorted
    },
    sunMiscUnsafeLazyValDetectAllPrint := printValues(sunMiscUnsafeLazyValDetectAll).value,
  )

  private def printValues(key: TaskKey[Seq[SunMiscUnsafeLazyValValue]]): Def.Initialize[Task[Unit]] = Def.task {
    implicit val converter: xsbti.FileConverter = (ThisBuild / fileConverter).value
    val result = key.value
    if (result.isEmpty) {
      println("not found sun.misc.Unsafe lazy val")
    } else {
      println(
        result.map { x =>
          val module = Seq[String](x.groupId, x.artifactId, x.version).map("\"" + _ + "\"").mkString(" % ")
          x.classNames.map { case (c, i) => s"  ${c} ${i}" }.mkString(
            s"${PluginCompat.toFile(x.path).getCanonicalPath}\n${module}\n",
            "\n",
            "\n"
          )
        }.mkString("")
      )
    }
  }

  override val projectSettings: Seq[Def.Setting[?]] = Def.settings(
    allConfig.flatMap { x =>
      Seq[Def.Setting[?]](
        x / sunMiscUnsafeLazyValDetectPrint := printValues(x / sunMiscUnsafeLazyValDetect).value,
        x / sunMiscUnsafeLazyValDetect := {
          val all = sunMiscUnsafeLazyValDetectAll.value
          (x / externalDependencyClasspath).value.flatMap { lib =>
            lib.get(PluginCompat.moduleIDStr).map(PluginCompat.parseModuleIDStrAttribute).flatMap { moduleId =>
              all.find(x =>
                (x.groupId == moduleId.organization) && (x.artifactId == moduleId.name) && (x.version == moduleId.revision)
              )
            }
          }.sorted
        }
      )
    }
  )

  // https://github.com/scala/scala3/blob/72c3a6629529031ac/library/src/scala/runtime/LazyVals.scala
  private val unsafeMethodNames: Set[String] = Set(
    "CAS",
    "get",
    "getOffset",
    "getOffsetStatic",
    "getStaticFieldOffset",
    "objCAS",
    "setFlag",
    "wait4Notification",
  )

  private def oldLazyValAndUnsafe(jarFilePath: File, directUnsafe: Boolean): List[(String, Int)] = {
    IO.withTemporaryDirectory { dir =>
      IO.unzip(jarFilePath, dir)
      (dir ** "*.class")
        .get()
        .toList
        .flatMap { classFile =>
          var counter: Int = 0
          Using.resource(new FileInputStream(classFile)) { in =>
            val reader = new ClassReader(in)
            val myVisitor = new ClassVisitor(ASM9) {
              override def visitMethod(
                access: Int,
                name: String,
                descriptor: String,
                signature: String,
                exceptions: Array[String]
              ): MethodVisitor = {
                new MethodVisitor(ASM9) {
                  override def visitMethodInsn(
                    opcode: Int,
                    owner: String,
                    name: String,
                    descriptor: String,
                    isInterface: Boolean
                  ): Unit = {
                    owner match {
                      case "scala/runtime/LazyVals$" if unsafeMethodNames(name) =>
                        counter += 1
                      case "sun/misc/Unsafe" if directUnsafe =>
                        counter += 1
                      case _ =>
                    }
                  }
                }
              }
            }
            reader.accept(myVisitor, 0)
            if (counter > 0) {
              Some(IO.relativize(dir, classFile).get -> counter)
            } else {
              None
            }
          }
        }
        .distinct
        .sorted
    }
  }
}

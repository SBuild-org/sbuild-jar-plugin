package org.sbuild.plugins.jar

import de.tototec.sbuild._
import de.tototec.sbuild.addons.support.ForkSupport
import org.sbuild.plugins.jar.internal.JarCreator
import scala.util.Success
import scala.util.Failure

class JarPlugin(implicit project: Project) extends Plugin[Jar] {

  override def create(name: String): Jar = {
    val targetDir = Path("target")
    val (jar, fileSets, phonyTarget) = name match {
      case "" | "main" =>
        (
          targetDir / "main.jar",
          Seq(
            JarFileSet.Dir(targetDir / "classes"),
            JarFileSet.Dir(Path("src/main/resources"))
          ),
            Some("jar")
        )
      case "test" =>
        (
          targetDir / "test.jar",
          Seq(JarFileSet.Dir(targetDir / "test-classes"),
            JarFileSet.Dir(Path("src/test/resources"))
          ),
            None
        )
      case x =>
        (
          targetDir / s"${x}.jar",
          Seq(),
          None
        )
    }

    Jar(
      jarFile = jar,
      fileSets = fileSets,
      manifest = Map(),
      phonyTarget = phonyTarget
    )
  }

  override def applyToProject(instances: Seq[(String, Jar)]): Unit = instances foreach {
    case (name, jar) =>

      val jarTarget = Target(jar.jarFile) dependsOn jar.deps ~~ jar.fileSets.foldLeft(TargetRefs())((l, r) => l ~ r.targetRefs) exec {
        new JarCreator().createJar(
          jarFile = jar.jarFile,
          fileSets = jar.fileSets.map { fs =>
            fs.baseDir -> fs.targetRefs.files
          },
          manifest = jar.manifest) match {
            case Success(_) =>
            case Failure(e) => throw e
          }
      }

      jar.phonyTarget.map { tName =>
        Target(s"phony:${tName}") dependsOn jarTarget
      }

  }

}
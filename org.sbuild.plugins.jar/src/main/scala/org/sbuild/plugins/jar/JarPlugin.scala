package org.sbuild.plugins.jar

import de.tototec.sbuild.Path
import de.tototec.sbuild.Plugin
import de.tototec.sbuild.Project
import de.tototec.sbuild.Target
import de.tototec.sbuild.TargetRef.fromString
import de.tototec.sbuild.addons.support.ForkSupport
import de.tototec.sbuild.toRichFile
import org.sbuild.plugins.jar.internal.JarCreator
import scala.util.Success
import scala.util.Failure

class JarPlugin(implicit project: Project) extends Plugin[Jar] {

  override def create(name: String): Jar = {
    val targetDir = Path("target")
    val (jar, fileSets) = name match {
      case "" | "main" =>
        (
          targetDir / "main.jar",
          Seq(
            JarFileSet.Dir(targetDir / "classes"),
            JarFileSet.Dir(Path("src/main/resources"))
          )
        )
      case "test" =>
        (
          targetDir / "test.jar",
          Seq(JarFileSet.Dir(targetDir / "test-classes"),
            JarFileSet.Dir(Path("src/test/resources"))
          )
        )
      case x => (targetDir / "${x}.jar", Seq())
    }

    Jar(
      jarFile = jar,
      fileSets = fileSets,
      manifest = Map()
    )
  }

  override def applyToProject(instances: Seq[(String, Jar)]): Unit = instances foreach {
    case (name, jar) =>
      
      Target(jar.jarFile) exec {
    	  new JarCreator().createJar(jar.jarFile, jar.fileSets, jar.manifest) match {
    	    case Success(_) => 
    	    case Failure(e) => throw e
    	  }
      }

  }
  
}
package org.sbuild.plugins.jar

import de.tototec.sbuild._
import java.io.File

trait JarFileSet {
  def baseDir: File
  //  def files: Seq[File]
  def targetRefs: TargetRefs
  def prefix: String = ""
}

object JarFileSet {
  
  case class Dir(override val baseDir: File)(implicit project: Project) extends JarFileSet {
    override def targetRefs: TargetRefs = s"scan:${baseDir}"
  }

  case class FromTargetRefs(override val baseDir: File, override val targetRefs: TargetRefs) extends JarFileSet

}
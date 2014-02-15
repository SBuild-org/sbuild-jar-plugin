package org.sbuild.plugins.jar

import de.tototec.sbuild._
import java.io.File

trait JarFileSet {
  def baseDir: File
  def files: Seq[File]
  def prefix: String = ""
}

object JarFileSet {

  case class Dir(dir: File) extends JarFileSet {
    override def baseDir: File = dir
    override def files: Seq[File] = dir.listFilesRecursive
  }

}
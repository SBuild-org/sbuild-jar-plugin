package org.sbuild.plugins.jar.internal

import org.sbuild.plugins.jar.JarFileSet
import scala.util.Try
import java.io.File
import java.util.jar.Manifest
import java.util.jar.Attributes
import java.io.BufferedOutputStream
import java.util.jar.JarOutputStream
import java.io.FileOutputStream
import scala.util.Success
import java.util.jar.JarEntry
import java.io.BufferedInputStream
import javax.imageio.stream.FileImageInputStream
import java.io.FileInputStream
import java.io.OutputStream
import java.io.InputStream

class JarCreator {

  def createJar(jarFile: File, fileSets: Seq[JarFileSet], manifest: Map[String, String]): Try[File] = {
    val m = new Manifest()
    val ma = m.getMainAttributes()
    ma.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    ma.putValue("Generated-With", "SBuild Jar Plugin 0.0.9000")
    manifest.foreach {
      case (k, v) => ma.putValue(k, v)
    }
    createJar(jarFile, fileSets, m)
  }

  def createJar(jarFile: File, fileSets: Seq[JarFileSet], manifest: Manifest): Try[File] = Try {
    // TODO: handle existing manifest file
    // TODO: handle multiple same resources
    // TODO: handle overwrite of jar file

    //    val createDirNodes = true
    //    var createdDirNodes: Seq[String] = Seq()

    val entries: Seq[(JarEntry, Option[File])] = fileSets.flatMap { fileSet =>
      val base = fileSet.baseDir.getPath
      val index = base.length + 1 /* separator */
      fileSet.files.flatMap { file =>
        val filePath = file.getPath
        if (!file.exists) {
          throw new IllegalArgumentException(s"""File "${filePath}" does not exist.""")
        }
        if (!filePath.startsWith(base)) {
          throw new IllegalArgumentException(s"""File "${filePath}" is not located under "${base}"""")
        }
        val nodePath = filePath.substring(index)

        //        if(createDirNodes) {
        //          file.getParentFile()
        //          
        //          
        //        }

        val entry = new JarEntry(nodePath)
        entry.setTime(file.lastModified)
        val streamable = if (file.isDirectory) None else Some(file)
        Seq(entry -> streamable)
      }
    }

    val explicitManifests = entries.filter { case (entry, _) => entry == "META-INF/MANIFEST.MF" }
    explicitManifests match {
      case Seq() =>
      case ms => // TODO Handle explicitly given Manifests
    }

    jarFile.getParentFile match {
      case null => // cwd
      case p => p.mkdirs
    }

    val outStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jarFile)), manifest)
    try {
      entries.foreach {
        case (entry, resource) =>
          outStream.putNextEntry(entry)
          resource.map { file =>
            val inStream = new BufferedInputStream(new FileInputStream(file))
            try {
              copyStream(inStream, outStream)
            } finally {
              inStream.close
            }
          }
      }

      jarFile
    } finally {
      outStream.close()
    }

  }

  def copyStream(in: InputStream, out: OutputStream): Unit = {
    val buf = new Array[Byte](1024)
    var len = 0
    while ({
      len = in.read(buf)
      len > 0
    }) {
      out.write(buf, 0, len)
    }
  }

}
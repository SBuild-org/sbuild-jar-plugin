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

  def createJar(jarFile: File, fileSets: Seq[(File, Seq[File])], manifest: Map[String, String]): Try[File] = {
    val m = new Manifest()
    val ma = m.getMainAttributes()
    ma.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    ma.putValue("Generated-With", "SBuild Jar Plugin 0.0.9000")
    manifest.foreach {
      case (k, v) => ma.putValue(k, v)
    }
    createJar(jarFile, fileSets, m)
  }

  def createJar(jarFile: File, fileSets: Seq[(File, Seq[File])], manifest: Manifest): Try[File] = Try {
    // TODO: handle existing manifest file
    // TODO: handle multiple same resources
    // TODO: handle overwrite of jar file

    //    val createDirNodes = true
    //    var createdDirNodes: Seq[String] = Seq()

    val entries: Seq[(JarEntry, Option[File])] = {
      var createdDirs: Set[String] = Set()
      (new JarEntry("META-INF/") -> None) +: fileSets.flatMap {
        case (baseDir, files) =>
          val base = baseDir.getPath
          val index = base.length + 1 /* separator */
          files.flatMap { file =>
            val filePath = file.getPath
            if (!file.exists) {
              throw new IllegalArgumentException(s"""File "${filePath}" does not exist.""")
            }
            if (!filePath.startsWith(base)) {
              throw new IllegalArgumentException(s"""File "${filePath}" is not located under "${base}"""")
            }

            val nodePath = filePath.substring(index)

            def findParents(relFile: File, trace: List[File] = Nil): List[File] = relFile.getParentFile match {
              case null => trace
              case p => findParents(p, p :: trace)
            }
            val parents = findParents(new File(nodePath))

            val parentEntries = parents.flatMap { dir =>
              val path = dir.getPath match {
                case p if p.endsWith("/") => p
                case p => p + "/"
              }
              if (createdDirs.contains(path)) Seq() else {
                createdDirs += path
                Seq(new JarEntry(path) -> None)
              }
            }

            parentEntries ++ (if (file.isDirectory()) {
              Seq(new JarEntry(nodePath match {
                case p if p.endsWith("/") => p
                case p => p + "/"
              }) -> None)
            } else {
              val entry = new JarEntry(nodePath)
              entry.setTime(file.lastModified)
              val streamable = if (file.isDirectory) None else Some(file)
              Seq(entry -> streamable)
            })

          }
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
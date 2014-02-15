package org.sbuild.plugins.jar

import java.io.File
import de.tototec.sbuild._

/**
 * Configuration for the SBuild Jar Plugin.
 *
 * Based on it's configuration, this plugin will register various targets.
 *
 */
case class Jar(
  jarFile: File,
  fileSets: Seq[JarFileSet],
  manifest: Map[String, String])


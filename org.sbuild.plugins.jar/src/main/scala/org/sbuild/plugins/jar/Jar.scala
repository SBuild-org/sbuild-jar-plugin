package org.sbuild.plugins.jar

import java.io.File
import de.tototec.sbuild._

/**
 * Configuration for the SBuild Jar Plugin.
 *
 * Based on it's configuration, this plugin will register various targets.
 *
 * @param jarFile The JAR file which will be created.
 * @param fileSets The `[[JarFileSets]]` denoting the content of the JAR file.
 * @param manifest Map of entries, which should be added to the JAR manifest.
 * @param deps Additional dependencies, which the resulting target should depend on (see `[[de.tototec.sbuild.Target#dependsOn]]`.
 * @param phonyTarget Name of an optional phony target, which also triggers the generation of the JAR.
 */
case class Jar(
  jarFile: File,
  fileSets: Seq[JarFileSet],
  manifest: Map[String, String],
  deps: TargetRefs = TargetRefs(),
  phonyTarget: Option[String] = None)


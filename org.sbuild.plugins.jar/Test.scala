import de.tototec.sbuild._

@version("0.7.1")
@classpath("target/org.sbuild.plugins.jar-0.0.9000.jar")
class Test(implicit _project: Project) {

  import org.sbuild.plugins.jar._
  Plugin[Jar]
  Plugin[Jar]("test")

  Plugin[Jar]("1") configure (_.copy(
    jarFile = Path("target/1.jar"),
    fileSets = Seq(JarFileSet.Dir(Path("target/classes")))
  ))

  Plugin[Jar]("2") configure (_.copy(
    fileSets = Seq(JarFileSet.Dir(Path("target/classes")))
  ))

}

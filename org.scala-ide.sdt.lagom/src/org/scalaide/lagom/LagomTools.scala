package org.scalaide.lagom

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.text.MessageFormat

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages

import com.typesafe.config.ConfigFactory

object noLagomLoaderPath {
  import org.scalaide.lagom.microservice.launching.LagomServerConfiguration._
  def apply(javaProject: IJavaProject): Boolean = try {
    val projectLocation = javaProject.getProject.getLocation
    val ProjectNameSegment = 1
    val configClassLoader = new URLClassLoader(javaProject.getResolvedClasspath(true).flatMap { cp =>
      Option(cp.getOutputLocation)
    }.distinct.map { icp =>
      projectLocation.append(icp.removeFirstSegments(ProjectNameSegment)).toFile.toURI.toURL
    } ++ Option(javaProject.getOutputLocation).map { o =>
      projectLocation.append(o.removeFirstSegments(ProjectNameSegment)).toFile.toURI.toURL
    }.toArray[URL])
    val projectConfig = ConfigFactory.load(configClassLoader)
    !projectConfig.hasPath(LagomApplicationLoaderPath)
  } catch {
    case allowToFailInRunner: Throwable => true
  }
}

object projectValidator {
  val LagomApplicationLoaderPath = "play.application.loader"

  private val noLagomLoaderPathInConfig: IProject => Boolean = (noLagomLoaderPath.apply _) compose (JavaCore.create _)

  def apply(name: String, setErrorMessage: String => Unit): PartialFunction[IProject, Boolean] = {
    PartialFunction.empty[IProject, Boolean].orElse[IProject, Boolean] {
      case project if !project.exists() =>
        setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_20, Array(name)))
        false
    }.orElse[IProject, Boolean] {
      case project if !project.isOpen =>
        setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_21, Array(name)))
        false
    }.orElse[IProject, Boolean] {
      case project if !project.hasNature(JavaCore.NATURE_ID) =>
        setErrorMessage(s"Project $name does not have Java nature.")
        false
    }.orElse[IProject, Boolean] {
      case project if noLagomLoaderPathInConfig(project) =>
        setErrorMessage(s"Project $name does not define $LagomApplicationLoaderPath path in configuration.")
        false
    }.orElse[IProject, Boolean] {
      case _ =>
        true
    }
  }
}

object maven {
  val MavenDelimeter = ":"
  private val / = File.separator
  val LocalRepoLocation = "target" + / + "local-repo"

  def dependencies(prjLocation: String)(groupId: String, artifactId: String, version: String): Seq[File] = {
    val locator = MavenRepositorySystemUtils.newServiceLocator()
    val system = newRepositorySystem(locator)
    val session = newSession(system, prjLocation)
    val artifact = new DefaultArtifact(Seq(groupId, artifactId, version).mkString(MavenDelimeter))
    val central = new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/").build()
    import scala.collection.JavaConverters._
    val collectRequest = new CollectRequest(new Dependency(artifact, JavaScopes.COMPILE), List(central).asJava)
    val filter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE)
    val request = new DependencyRequest(collectRequest, filter)
    val result = system.resolveDependencies(session, request)
    result.getArtifactResults.asScala.map { artifact =>
      artifact.getArtifact.getFile
    }.toSeq
  }

  private def newRepositorySystem(locator: DefaultServiceLocator): RepositorySystem = {
    locator.addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory])
    locator.addService(classOf[TransporterFactory], classOf[FileTransporterFactory])
    locator.addService(classOf[TransporterFactory], classOf[HttpTransporterFactory])
    locator.getService(classOf[RepositorySystem])
  }

  private def newSession(system: RepositorySystem, rootLocation: String): RepositorySystemSession = {
    val session = MavenRepositorySystemUtils.newSession()
    val localRepo = new LocalRepository(rootLocation + / + LocalRepoLocation)
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo))
    session
  }
}

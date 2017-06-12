package org.scalaide.lagom.launching

import scala.util.Try

import com.lightbend.lagom.scaladsl.server.LagomApplicationContext
import com.lightbend.lagom.scaladsl.server.LagomApplicationLoader
import com.lightbend.lagom.scaladsl.server.RequiresLagomServicePort

import akka.persistence.cassandra.testkit.CassandraLauncher
import play.api.ApplicationLoader.Context
import play.api.Configuration
import play.api.Environment
import play.api.Mode
import play.api.Play
import play.core.DefaultWebCommands
import play.core.server.ServerConfig
import play.core.server.ServerProvider
import java.time.format.DateTimeFormatter
import java.nio.file.Files
import org.apache.cassandra.io.util.FileUtils
import com.lightbend.lagom.scaladsl.persistence.cassandra.testkit.TestUtil
import java.time.LocalDateTime

object LagomLauncher {
  val LagomLauncherClass = "org.scalaide.lagom.launching.LagomLauncher$"
  val LagomClassSwitch = "lagomclass"

  def main(args: Array[String]): Unit = {
    try {
      args.collectFirst {
        case n if n.startsWith(LagomClassSwitch) =>
          n.replaceFirst(LagomClassSwitch, "")
      }.foreach { className =>
        import scala.reflect.runtime.{ universe => ru }
        val mirror = ru.runtimeMirror(getClass.getClassLoader)
        val classSymbol = mirror.classSymbol(Class.forName(className))
        val isDefaultConstructor: PartialFunction[ru.Symbol, Boolean] = {
          case c if c.isConstructor =>
            c.asMethod.paramLists match {
              case Nil => true
              case h +: Nil if h.isEmpty => true
              case _ => false
            }
          case _ => false
        }
        classSymbol.typeSignature.members.collectFirst {
          case c if isDefaultConstructor(c) =>
            c.asMethod
        }.map { constructor =>
          val classToInstantiate = mirror.reflectClass(classSymbol)
          val toInvoke = classToInstantiate.reflectConstructor(constructor)
          val a = classSymbol.toType
          toInvoke().asInstanceOf[LagomApplicationLoader]
        }.map { appLoader =>
          val config = Configuration.from(Map("lagom.service-locator" -> Map("url" -> "http://127.0.0.1:3467")))
          
          val cassConfig = {
            val now = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS").format(LocalDateTime.now())
            val testName = s"ServiceTest_$now"
            val cassandraPort = CassandraLauncher.randomPort
            val cassandraDirectory = Files.createTempDirectory(testName).toFile
            FileUtils.deleteRecursiveOnExit(cassandraDirectory)
            CassandraLauncher.start(cassandraDirectory, "lagom-test-embedded-cassandra.yaml", clean = false, port = 0)
            Configuration(TestUtil.persistenceConfig(testName, cassandraPort, useServiceLocator = false)) ++
              Configuration("lagom.cluster.join-self" -> "on")
          }

          val context = LagomApplicationContext(Context(Environment.simple(mode = Mode.Dev), None, new DefaultWebCommands, config ++ cassConfig))
          appLoader.logger.error("################ starting... ###############")
          try {
          val lagomApplication = appLoader.loadDevMode(context)
          appLoader.logger.error("!!!!!!!!" + lagomApplication.serviceInfo.serviceName)
          Play.start(lagomApplication.application)
          val serverConfig = ServerConfig(port = Some(9099), mode = lagomApplication.environment.mode)
          val playServer = ServerProvider.defaultServerProvider.createServer(serverConfig, lagomApplication.application)
          lagomApplication match {
            case requiresPort: RequiresLagomServicePort =>
              requiresPort.provideLagomServicePort(playServer.httpPort.orElse(playServer.httpsPort).get)
            case other => ()
          }
          Runtime.getRuntime.addShutdownHook {
            new Thread { () =>
              Try(Play.stop(lagomApplication.application))
              Try(playServer.stop())
              Try(CassandraLauncher.stop())
            }
          }
          } catch {
            case e: Error => appLoader.logger.error("!!!!!!!!!!!" + e)
          }
        }
      }
    } catch {
      case e: Throwable => e.printStackTrace()
    } finally {
    }
  }
}

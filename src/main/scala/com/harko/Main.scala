package com.harko

import java.lang.management.ManagementFactory

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats

import org.json4s.jackson.Serialization.write
import org.json4s.jackson.Serialization.read
import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext}

import org.slf4j.LoggerFactory
import akka.http.scaladsl.{ConnectionContext, Http}

class Main

object Main extends App  {

  val log = LoggerFactory.getLogger(classOf[Main])

  implicit val formats = DefaultFormats

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val httpConfig = config.getConfig("http")

  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val routes: Route = pathSingleSlash {
    get {
      entity(as[String]) {
        (str: String) => {
          complete {
            log.debug(str)
            StatusCodes.OK
          }
        }
      }
    }
  }
  Http().bindAndHandle(routes, httpInterface, httpPort)
}

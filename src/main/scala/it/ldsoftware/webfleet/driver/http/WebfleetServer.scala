package it.ldsoftware.webfleet.driver.http

import scala.concurrent.duration._
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.{Done, actor => classic}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

class WebfleetServer(routes: Route, port: Int, system: ActorSystem[_]) extends LazyLogging {
  import akka.actor.typed.scaladsl.adapter._
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  private val shutdown = CoordinatedShutdown(classicSystem)
  import system.executionContext

  def start(): Unit = {
    Http()
      .bindAndHandle(routes, "localhost", port)
      .onComplete {
        case Success(binding) =>
          logger.info(s"Webfleet Driver listening on http port $port")
          shutdown.addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "http-graceful-terminate") { () =>
            binding.terminate(10.seconds).map { _ =>
              logger.info("Graceful shutdown completed")
              Done
            }
          }
        case Failure(exception) =>
          logger.error(s"Failed to bind routes on port $port", exception)
          system.terminate()
      }
  }

}

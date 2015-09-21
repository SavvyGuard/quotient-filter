package com.videoamp.quotient_filter

import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem("quiz-management-service")
  val messages = system.actorOf(Props(new Messages()), "controller")
  val host = ""
  val port = 8080
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10 seconds)
  IO(Http).ask(Http.Bind(listener = messages, interface = host, port = port))
    .mapTo[Http.Event]
    .map {
      case Http.Bound(address) =>
        println(s"REST interface bound to $address")
      case Http.CommandFailed(cmd) =>
        println("REST interface could not bind to " +
          s"$host:$port, ${cmd.failureMessage}")
        system.shutdown()
    }
}

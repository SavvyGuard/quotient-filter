package com.videoamp.quotient_filter

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure
import akka.pattern.ask
import spray.routing._
import spray.http._
import MediaTypes._

class Messages extends HttpServiceActor{

  implicit val timeout = Timeout(5)
  override def preStart(): Unit = {
  }
  def receive = runRoute {
    pathPrefix("query") {
      pathPrefix( Segment ) { filterId =>
        path( Rest ) { queryId =>
          get {
            println(filterId)
            println(queryId)
            val uuid  = queryId.filterNot("-" contains _)
            val filter = context.actorSelection("/user/controller/" + filterId)
            println(uuid)
            onComplete((filter ? Command.Query(uuid)).mapTo[Int]) {
              case Success(value) =>
                complete(s"$value")
              case Failure(value) =>
                println("failure")
                complete(s"$value")
              case _ =>
                println("error")
                complete("Error")
            }
          }
        }
      }
    } ~
    pathPrefix("insert") {
      pathPrefix( Segment ) { filterId =>
        path( Rest ) { queryId =>
          get {
            println(filterId)
            println(queryId)
            val uuid  = queryId.filterNot("-" contains _)
            val filter = context.actorSelection("/user/controller/" + filterId)
            println(uuid)
            onComplete((filter ? Command.Insert(uuid)).mapTo[Int]) {
              case Success(value) =>
                complete(s"$value")
              case Failure(value) =>
                println("failure")
                complete(s"$value")
              case _ =>
                println("error")
                complete("Error")
            }
          }
        }
      }
    } ~
    path("new" / Rest) { queryId =>
      get {
        context.actorOf(Props[QuotientFilterAccessActor], queryId)
        complete(queryId)
      }
    }
  }

}


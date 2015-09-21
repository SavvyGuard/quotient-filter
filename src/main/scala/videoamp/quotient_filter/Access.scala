package com.videoamp.quotient_filter

import akka.actor.Actor
import akka.actor.Props

class QuotientFilterAccessActor extends Actor {

  val quotientFilter = new QuotientFilter()
  def receive = {
    case Command.Query(value) =>
      val times_seen = quotientFilter.isMember(value)
      println("returning query result")
      println(times_seen)
      sender() ! times_seen
      println("value sent")
    case Command.Insert(value) =>
      val times_seen = quotientFilter.insert(value)
      println("returning insertion result")
      println(times_seen)
      sender() ! times_seen
  }
}

object QuotientFilterAccessActor {
  def props = {
    Props(new QuotientFilterAccessActor)
  }
}

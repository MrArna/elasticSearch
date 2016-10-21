package Actors

import Messages.{Calculate, PiApproximation, Result, Work}
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool

import scala.concurrent.duration.Duration

/**
  * Created by Marco on 21/10/16.
  */
class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef) extends Actor
{
    var pi: Double = _
    var nrOfResults: Int = _
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

    def receive = {
      case Calculate ⇒
        for (i ← 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
      case Result(value) ⇒
        pi += value
        nrOfResults += 1
        if (nrOfResults == nrOfMessages) {
          // Send the result to the listener
          listener ! PiApproximation(pi, duration = Duration((System.currentTimeMillis - start), "millis"))
          // Stops this actor and all its supervised children
          context.stop(self)
        }
    }
}

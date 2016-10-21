package Actors

import Messages.{Result, Work}
import akka.actor.Actor

/**
  * Created by Marco on 21/10/16.
  */
class Worker extends Actor
{
    def calculatePiFor(start: Int, nrOfElements: Int): Double = {
      var acc = 0.0
      for (i ← start until (start + nrOfElements))
        acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
      acc
    }

    def receive = {
      case Work(start, nrOfElements) ⇒
        sender ! Result(calculatePiFor(start, nrOfElements)) // perform the work
    }


}

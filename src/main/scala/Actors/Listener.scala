package Actors

import Messages.PiApproximation
import akka.actor.Actor

/**
  * Created by Marco on 21/10/16.
  */
class Listener extends Actor
{
  def receive = {
    case PiApproximation(pi, duration) ⇒
      println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s"
        .format(pi, duration))
      context.system.terminate();
  }

}

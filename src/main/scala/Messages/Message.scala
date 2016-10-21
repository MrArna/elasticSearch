package Messages

import scala.concurrent.duration.Duration

/**
  * Created by Marco on 21/10/16.
  */
sealed trait Message
case object Calculate extends Message
case class Work(start: Int, nrOfElements: Int) extends Message
case class Result(value: Double) extends Message
case class PiApproximation(pi: Double, duration: Duration)

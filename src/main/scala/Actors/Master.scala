package Actors

import Messages._
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool

/**
  * Created by Marco on 21/10/16.
  */
class Master(init: Int, nrOfWorkers: Int, nrOfProjects:Int, sinker: ActorRef) extends Actor
{
    var nrOfDownload: Int = _
    val start: Long = System.currentTimeMillis

    //val workerRouter = context.actorOf(
      //Props[Worker].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

    val downloaderRouter = context.actorOf(
      Props[Downloader].withRouter(RoundRobinPool(nrOfWorkers)), name = "downloaderRouter")

    val uploaderRouter = context.actorOf(
      Props[Uploader].withRouter(RoundRobinPool(nrOfWorkers)), name = "uploaderRouter")

    def receive = {
      case Start ⇒
        println("-> Master Started wih: " + nrOfWorkers + " " + init + " " + nrOfProjects)
        for(id <- init until (init + nrOfProjects)) {
          downloaderRouter ! DownloadProject(id)
        }

      case DownloadedProject(project) ⇒
        uploaderRouter ! Send(project)

      case Sent ⇒
        nrOfDownload += 1
        if (nrOfDownload == nrOfProjects) {
          // Send the result to the sinker
          sinker ! End
          // Stops this actor and all its supervised children
          println("-> Master Ended")
          context.stop(self)
        }
    }
}

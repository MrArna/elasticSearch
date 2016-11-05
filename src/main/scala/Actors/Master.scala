package Actors

import Messages._
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool

/**
  * Created by Marco on 21/10/16.
  */
class Master(init: Int, nrOfWorkers: Int, nrOfProjects:Int, sinker: ActorRef) extends Actor
{
    var nrOfDownload: Int = 0
    var nrOfDowloadFailed: Int = 0
    val start: Long = System.currentTimeMillis

    val downloaderRouter = context.actorOf(
      Props[Downloader].withRouter(RoundRobinPool(nrOfWorkers)), name = "downloaderRouter")

    val uploaderRouter = context.actorOf(
      Props[Uploader].withRouter(RoundRobinPool(nrOfWorkers)), name = "uploaderRouter")

    val parserRouter = context.actorOf(
      Props[Parser].withRouter(RoundRobinPool(nrOfWorkers)), name = "parserRouter")


    def receive = {
      case Start ⇒
        println("-> Master Started wih: " + nrOfWorkers + " " + init + " " + nrOfProjects)
        for(id <- init until (init + nrOfProjects)) {
          downloaderRouter ! DownloadProject(id)
        }

      case Parse(prjId,json) ⇒
        println("-> Master send \"Parse\" with: " +prjId)
        parserRouter ! Parse(prjId,json)

      case Parsed(source) ⇒
        println("-> Master received \"Parsed\" and forward it ")
        uploaderRouter ! Send(source)

      case DownloadFailced =>
        nrOfDowloadFailed += 1
        if ((nrOfDownload + nrOfDowloadFailed) == nrOfProjects) {
          // Send the result to the sinker
          sinker ! End
          // Stops this actor and all its supervised children
          println("-> Master Ended")
          context.stop(self)
          System.exit(0)
        }

      case Sent ⇒
        nrOfDownload += 1
        //println("Project attempted = " + (nrOfDownload + nrOfDowloadFailed) + ", completed: " + nrOfDownload + ", failed: " + nrOfDowloadFailed)
        if ((nrOfDownload + nrOfDowloadFailed) == nrOfProjects) {
          // Send the result to the sinker
          sinker ! End
          // Stops this actor and all its supervised children
          println("-> Master Ended")
          context.stop(self)
          System.exit(0)
        }
    }
}

package App

import Actors.{Sinker, Master}
import Messages.{Start}
import akka.actor.{ActorSystem, Props}

/**
  * Created by Marco on 21/10/16.
  */
object Elastic {

  val usage = """
    Usage:   -i <starting-project-id> -n <nr-of-projects>  -w <nr-of-workers>
              """

  type OptionMap = Map[Symbol, Any]


  //read args and map values
  def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
    def isSwitch(s : String) = (s(0) == '-')
    list match {
      case Nil => map
      case "-i" :: value :: tail => nextOption(map ++ Map('init -> value.toInt), tail)
      case "-n" :: value :: tail => nextOption(map ++ Map('nrOfProjects -> value.toInt), tail)
      case "-w" :: value :: tail => nextOption(map ++ Map('workers -> value.toInt), tail)
      case string :: opt2 :: tail if isSwitch(opt2) => nextOption(map ++ Map('infile -> string), list.tail)
      case string :: Nil =>  nextOption(map ++ Map('infile -> string), list.tail)
    }
  }




  def main(args: Array[String]) {
    if (args.length == 0) {
      println(usage)
      System.exit(0)
    }
    val arglist = args.toList

    val options = nextOption(Map(), arglist)

    val system = ActorSystem("ElasticSystem") //init system
    val sinker = system.actorOf(Props[Sinker], name = "Sinker") //init sinker

    // create the master
    val master = system.actorOf(Props(new Master(
                                                  init = options.get('init).get.asInstanceOf[Int],
                                                  nrOfProjects = options.get('nrOfProjects).get.asInstanceOf[Int],
                                                  nrOfWorkers = options.get('workers).get.asInstanceOf[Int],
                                                  sinker = sinker)
                                                ), name = "master")
    // start the actor system
    master ! Start



  }

}


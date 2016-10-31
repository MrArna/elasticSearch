package Actors

import Messages.{DownloadAnalitycs}
import akka.actor.Actor

/**
  * Created by Marco on 21/10/16.
  */
class Sinker extends Actor
{
  def receive = {
    case DownloadAnalitycs(project) ⇒
      context.system.terminate();
  }

}

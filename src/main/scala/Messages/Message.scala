package Messages

import org.json4s.JsonAST.JValue

/**
  * Created by Marco on 21/10/16.
  */
sealed trait Message
case object Start extends Message
case class DownloadProject(nrOfProjects: Int) extends Message
case class DownloadedProject(project: JValue) extends Message
case class DownloadAnalitycs(project: Any) extends Message
case class Send(project: JValue) extends Message
case object Sent extends Message
case object  End extends Message
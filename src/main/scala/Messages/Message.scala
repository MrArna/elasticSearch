package Messages

/**
  * Created by Marco on 21/10/16.
  */
sealed trait Message
case object Start extends Message
case class DownloadProject(nrOfProjects: Int) extends Message
case class DownloadAnalitycs(project: Any) extends Message
case class Send(project: String) extends Message
case object Sent extends Message
case object  End extends Message
case class Parse(projectId: String, json: String) extends Message
case class Parsed(json: String) extends Message
case object DownloadFailced extends Message

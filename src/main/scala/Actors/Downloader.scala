package Actors

import Messages.{DownloadProject, DownloadedProject}
import akka.NotUsed
import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import org.json4s.JsonAST.JValue
import org.json4s.Xml._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.xml.XML

/**
  * Created by Marco on 31/10/16.
  */
class Downloader extends Actor {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  final implicit val executionContext = context.system.dispatcher
  final val apiKey = "32da910f07ce15d41daefc42a1562fd2de9756ab7d6292fe6c79c8cedc55c2ef&v=1"


  def request(nrOfPrj: Int):JValue = {
    val http = Http(context.system)
    import HttpMethods._

    val userData = ByteString("abc")

    val request:HttpRequest=
      HttpRequest(
        GET,
        uri = "https://www.openhub.net/projects/" + nrOfPrj + ".xml?api_key=" + apiKey
      )

    val fut : Future[HttpResponse] = http.singleRequest(request)

    val response = Await.result(fut,Duration.Inf)

    val src : Source[ByteString,Any] = response.entity.dataBytes
    val stringFlow : Flow[ByteString,String, NotUsed] = Flow[ByteString].map(chunk => chunk.utf8String)
    val sink : Sink[String,Future[String]] = Sink.fold("")(_ + _)

    val content : RunnableGraph[Future[String]] = (src via stringFlow toMat sink) (Keep.right)

    val aggregation : Future[String] = content.run()

    Await.result(aggregation,Duration.Inf)

    //println(aggregation.value)

    val xml = XML.loadString(aggregation.value.get.get)

    return toJson(xml \\ "project")

    //var list : List[JValue] = List()
    //for(account <- xml \\ "account") list = list :+ toJson(account).removeField { _ == JField("badges",JNothing)}


    //println(prettyJson(list(1)))


  }





  override def receive: Receive = {

    case DownloadProject(nrOfProjects) ⇒
      println("--> Downloader" + context.self.toString() + "Started")
      sender ! DownloadedProject(request(nrOfProjects))


  }



}

package Actors

import Messages.{DownloadFailced, DownloadProject, Parse}
import akka.NotUsed
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import org.json4s.JsonAST.JValue
import org.json4s.Xml._
import org.json4s.{DefaultFormats, jackson}

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
  implicit val formats = DefaultFormats


  var json:JValue = _


  def request(nrOfPrj: Int) = {
    val http = Http(context.system)
    import HttpMethods._

    val userData = ByteString("abc")


    //create and make request
    val request:HttpRequest=
      HttpRequest(
        GET,
        uri = "https://www.openhub.net/projects/" + nrOfPrj + ".xml?api_key=" + apiKey
      )

    val fut : Future[HttpResponse] = http.singleRequest(request)

    val response = Await.result(fut,Duration.Inf)


    //create and launch a flow in order to parse the response
    val src : Source[ByteString,Any] = response.entity.dataBytes
    val stringFlow : Flow[ByteString,String, NotUsed] = Flow[ByteString].map(chunk => chunk.utf8String)
    val sink : Sink[String,Future[String]] = Sink.fold("")(_ + _)

    val content : RunnableGraph[Future[String]] = (src via stringFlow toMat sink) (Keep.right)

    val aggregation : Future[String] = content.run()

    Await.result(aggregation,Duration.Inf)

    //println(aggregation.value)

    val xml = XML.loadString(aggregation.value.get.get)

    //println(jackson.prettyJson(toJson(xml \\ "project")))


    //return a null json if some error occurs
    json = null
    if((xml \\ "error").isEmpty)
      json =  toJson(xml \\ "project")


    //println(xml)


  }





  override def receive: Receive = {

    case DownloadProject(nrOfProjects) ⇒
      println("--> Downloader " + "Started")
      request(nrOfProjects)
      if (json != null){
        sender ! Parse((json \ "project" \ "id").extract[String], jackson.prettyJson(json))
      }
      else{
        sender ! DownloadFailced
      }


  }



}

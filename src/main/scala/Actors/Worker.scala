package Actors

import Messages.{DownloadProject, DownloadedProject, Send}
import akka.NotUsed
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import org.json4s.JsonAST.JValue
import org.json4s.Xml.toJson
import org.json4s.jackson._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.xml.XML


/**
  * Created by Marco on 21/10/16.
  */



class Worker extends Actor
{

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

      println(aggregation.value)

      val xml = XML.loadString(aggregation.value.get.get)

      return toJson(xml \\ "project")

      //var list : List[JValue] = List()
      //for(account <- xml \\ "account") list = list :+ toJson(account).removeField { _ == JField("badges",JNothing)}


      //println(prettyJson(list(1)))


    }

    def send(data: JValue) =
    {
      val http = Http(context.system)
      import HttpMethods._

      //val userData = ByteString("{\"akka\":\"testFromAkka\"}")

      val auth = headers.Authorization(BasicHttpCredentials("user","BwxuUA27"))


      //println(prettyJson(data \ "project"))

      val request:HttpRequest=
        HttpRequest(
          POST,
          uri = "http://146.148.93.156:80/elasticsearch/projects/none",
          headers = List(auth),
          entity = HttpEntity(ContentTypes.`application/json`,prettyJson(data \ "project"))

        )
      val fut : Future[HttpResponse] = http.singleRequest(request)

      val response = Await.result(fut,Duration.Inf)

      val src : Source[ByteString,Any] = response.entity.dataBytes
      val stringFlow : Flow[ByteString,String, NotUsed] = Flow[ByteString].map(chunk => chunk.utf8String)
      val sink : Sink[String,Future[String]] = Sink.fold("")(_ + _)

      val content : RunnableGraph[Future[String]] = (src via stringFlow toMat sink) (Keep.right)

      val aggregation : Future[String] = content.run()

      Await.result(aggregation,Duration.Inf)

      print(aggregation.value)
    }




    def receive = {
      case DownloadProject(nrOfProjects) ⇒
        println("DownloadStarted")
        sender ! DownloadedProject(request(nrOfProjects))

      case Send(data) ⇒
        println("Send data started")
        send(data)


    }


}

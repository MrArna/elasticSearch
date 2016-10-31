package Actors

import Messages.{Send, Sent}
import akka.NotUsed
import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import org.json4s.JsonAST.JValue
import org.json4s.jackson._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Marco on 31/10/16.
  */
class Uploader extends Actor {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  final implicit val executionContext = context.system.dispatcher

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

    //println(aggregation.value.get.get)
  }




  override def receive: Receive = {

    case Send(data) ⇒
      println("--> Uploader" + context.self.toString() + "Started")
      sender ! Sent

  }

}

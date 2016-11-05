package Actors

import java.io.File
import java.nio.charset.MalformedInputException

import Messages.{Parse, Parsed}
import akka.NotUsed
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process._
import scala.xml.XML



/**
  * Created by Marco on 01/11/16.
  */
class Parser extends Actor {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  final implicit val executionContext = context.system.dispatcher
  final val apiKey = "32da910f07ce15d41daefc42a1562fd2de9756ab7d6292fe6c79c8cedc55c2ef&v=1"
  implicit val formats = DefaultFormats


  // process a file content only if is readable as text
  def processFile(f: File): String = {
    try {
      val lines = scala.io.Source.fromFile(f).getLines.toArray
      var src: String = ""
      for (i <- 0 to lines.length - 1) {
        src = src + " " + lines(i)
      }
      return src
    } catch {
        case ex : MalformedInputException => return ""
    }
  }

  //recursive visit of a folder
  def recursiveListFiles(f: File): String = {
    val these = f.listFiles.filter(!_.isHidden)
    var src: String = ""
    if (these != null) {
      for (i <- 0 to these.length - 1) {
        if (these(i).isFile) {
          src = src + " " + processFile(these(i))
        }
        if (these(i).isDirectory) {
          src = src + " " + recursiveListFiles(these(i))
        }
      }
      return src
    }
    else {
      return ""
    }
  }


  def parsing(projectId: String, json: String): String = {

    //create and make the http request
    val http = Http(context.system)
    import HttpMethods._

    val request:HttpRequest=
      HttpRequest(
        GET,
        uri = "https://www.openhub.net/projects/" + projectId + "/enlistments.xml?api_key=" + apiKey
      )

    val fut : Future[HttpResponse] = http.singleRequest(request)

    val response = Await.result(fut,Duration.Inf)

    //flow to analyze the response
    val src : Source[ByteString,Any] = response.entity.dataBytes
    val stringFlow : Flow[ByteString,String, NotUsed] = Flow[ByteString].map(chunk => chunk.utf8String)
    val sink : Sink[String,Future[String]] = Sink.fold("")(_ + _)

    val content : RunnableGraph[Future[String]] = (src via stringFlow toMat sink) (Keep.right)

    val aggregation : Future[String] = content.run()

    Await.result(aggregation,Duration.Inf)

    // response into xml
    val xml = XML.loadString(aggregation.value.get.get)

    var source : String = ""


    // if no error and the repo is a svn then clone, else notify in the field
    if (!(xml \\ "error").isEmpty) {
      println(xml \\ "error")
      source =  "No public repo available"
      val newJson = (parseJson(json) \ "project") merge JObject(List(("sourceCode",JString(source))))
      return prettyJson(newJson)
    }

    val vsc = (xml \\ "type") text


    if (vsc == "SvnSyncRepository"){
      val cloneProcess = Process("git svn clone " + (xml \\ "url").text + " " + projectId + "/")
      val p = cloneProcess.run()
      val exitVal = p.exitValue()
      println("clone completed with " + exitVal)
      if (exitVal == 0){
        source = recursiveListFiles(new File(projectId + "/"))
      }
      else
      {
        source = "not able to clone the " + vsc + " repository"
      }
    }
    else {
      source = "not able to clone the " + vsc + " repository"
    }

    //remove the local file
    "rm -rf ./"  + projectId !!;

    val newJson = (parseJson(json) \ "project") merge JObject(List(("sourceCode",JString(source))))

    //println("Json: \n\n\n\n " + prettyJson(newJson))

    return prettyJson(newJson)
  }


  override def receive: Receive = {

    case Parse(id,json) ⇒
      println("--> Parser started with: " + id)

      //println(parsing(id,json))

      sender ! Parsed(parsing(id,json))

  }


}

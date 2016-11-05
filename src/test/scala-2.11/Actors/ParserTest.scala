package Actors

import Messages.{Parse, Parsed}
import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.json4s.jackson._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.sys.process._



/**
  * Created by Marco on 02/11/16.
  */





class ParserTest extends TestKit(ActorSystem("ParserTest",ConfigFactory.parseString(TestKitUsageSpec.config)))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
{


  "A ParserActor" should {
    "download the repo and return a string containing the content of the files" in {
      val actorRef = TestActorRef[Parser]


      actorRef ! Parse("36011","{\"test\":\"test\"}")

      expectMsgClass(classOf[Parsed])

    }

  }

}

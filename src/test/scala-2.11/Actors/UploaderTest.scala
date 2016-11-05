package Actors

import Messages.{Parse, Parsed}
import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers, WordSpecLike}

/**
  * Created by Marco on 05/11/16.
  */
class UploaderTest extends TestKit(ActorSystem("ParserTest",ConfigFactory.parseString(TestKitUsageSpec.config)))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
{
  "An Uploader Actor" should {
    "download the repo and return a string containing the content of the files" in {
      val actorRef = TestActorRef[Parser]


      actorRef ! Parse("36011", "{\"test\":\"test\"}")

      expectMsgClass(classOf[Parsed])

    }
  }

}

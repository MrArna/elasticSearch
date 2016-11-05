package Actors

import Messages.{DownloadProject, Parse}
import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers, WordSpecLike}






class DownloaderTest extends TestKit(ActorSystem("DownloadTest",ConfigFactory.parseString(TestKitUsageSpec.config)))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
{

  "A Downloader" should {
    "download the metadata and send a parse message" in {
      val actorRef = TestActorRef[Downloader]


      for(id <- 36001 to 36001 + 10)
        actorRef ! DownloadProject(id)

      expectMsgClass(classOf[Parse])

    }

  }

}

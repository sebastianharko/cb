package com.harko

import java.lang.management.ManagementFactory

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{DateTime, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.persistence.PersistentActor
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import akka.stream.ActorMaterializer
import com.harko.MemberAccountant.{DisplayBillingHistory, Invoice, BillingEvent, AskMemberForPayment}
import com.harko.MemberState.Unregistered
import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats

import org.json4s.jackson.Serialization.write
import org.json4s.jackson.Serialization.read
import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext}

import org.slf4j.LoggerFactory
import akka.http.scaladsl.{ConnectionContext, Http}

import scala.collection.mutable.ArrayBuffer

class Main

object Main extends App  {

  val log = LoggerFactory.getLogger(classOf[Main])

  log.info("""  ______    __    __       __  ___________      _______     ______  ___________
             | /" _  "\  /" |  | "\     /""\("     _   ")    |   _  "\   /    " \("     _   ")
             |(: ( \___)(:  (__)  :)   /    \)__/  \\__/     (. |_)  :) // ____  \)__/  \\__/
             | \/ \      \/      \/   /' /\  \  \\_ /        |:     \/ /  /    ) :)  \\_ /
             | //  \ _   //  __  \\  //  __'  \ |.  |        (|  _  \\(: (____/ //   |.  |
             |(:   _) \ (:  (  )  :)/   /  \\  \\:  |        |: |_)  :)\        /    \:  |
             | \_______) \__|  |__/(___/    \___)\__|        (_______/  \"_____/      \__|
             |                                                                        """.stripMargin)

  implicit val system: ActorSystem = ActorSystem()
  val membActRef = system.actorOf(MemberAccountant.props("User-42"))
  membActRef ! AskMemberForPayment(10, "CAD")

}

/*
A member in a club can be unregistered, registered or banned.
Usually, a member becomes registered by paying the membership fee.
 */
object MemberState {

  sealed trait MemberState
  case object Unregistered extends MemberState
  case object Registered extends MemberState
  case object Banned extends MemberState

}

object MemberAccountant {

  def props(userId:String): Props = Props(new MemberAccountant(userId))

  sealed trait Command
  case class AskMemberForPayment(amount:Int, currency: String) extends Command
  case object DisplayBillingHistory

  sealed trait Event
  sealed trait BillingEventType
  case object Invoice extends BillingEventType
  case object Payment extends BillingEventType
  case class BillingEvent(`type`:BillingEventType, amount:Double, currency: String, date: DateTime) extends Event

}

class MemberAccountant(userId: String) extends PersistentActor {

  var state = Unregistered
  val billingHistory = new ArrayBuffer[BillingEvent]()

  override def receiveRecover: Receive = {
    case p:BillingEvent => {
      billingHistory += p
    }
  }

  override def receiveCommand: Receive = {

    case AskMemberForPayment(amount, currency) => {
      persist(BillingEvent(Invoice, amount, currency, DateTime.now)) {
        (event: BillingEvent) => {
          billingHistory += event
        }
      }
    }
    case DisplayBillingHistory => {
      val html = <html>
        <table border = "1">
          <tr>
            <th>Event</th>
            <th>Amount</th>
            <th>Date</th>
          </tr>
        </table>
      </html>
      sender() ! html.toString()
    }
  }

  override def persistenceId: String = userId
}

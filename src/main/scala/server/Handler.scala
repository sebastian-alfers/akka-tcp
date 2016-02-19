package server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString
import scala.concurrent.duration._
import scala.collection.mutable.Queue

object Handler{
  def props(connection: ActorRef, remote: InetSocketAddress) = Props(new Handler(connection, remote))

}

class Handler(connection: ActorRef, remote: InetSocketAddress) extends Actor with ActorLogging {

  val storage = new Queue[ByteString]
  var stored = 0
  var transferred = 0

  val maxStored = 100
  val highWatermark = 80
  val lowWatermark = 20
  var suspended = false
  var closing = false

  import Tcp._

  // sign death pact: this actor terminates when connection breaks
  context watch connection

  case object Ack extends Event

  import context.dispatcher

  context.system.scheduler.scheduleOnce(5 seconds, connection, Write(ByteString("jea"), Ack))
  context.system.scheduler.scheduleOnce(6 seconds, connection, Write(ByteString("jea"), Ack))
  context.system.scheduler.scheduleOnce(7 seconds, connection, Write(ByteString("jea"), Ack))
  context.system.scheduler.scheduleOnce(8 seconds, connection, Write(ByteString("jea"), Ack))
  context.system.scheduler.scheduleOnce(3 seconds, connection, Close)


  def receive = {
    case Received(data) =>
      buffer(data)
      connection ! Write(data, Ack)

      context.become({
        case Received(data) => buffer(data)
        case Ack            => acknowledge()
        case PeerClosed     => closing = true
      }, discardOld = false)

    case PeerClosed => context stop self
  }

  private def buffer(data: ByteString): Unit = {
    storage += data
    stored += data.size

    if (stored > maxStored) {
      log.error(s"drop connection to [$remote] (buffer overrun)")
      context stop self
    } else if (stored > highWatermark) {
      log.debug(s"suspending reading")
      connection ! SuspendReading
      suspended = true
    }
  }

  private def acknowledge(): Unit = {
    require(storage.nonEmpty, "storage was empty")

    val size = storage.head.size
    stored -= size
    transferred += size

    storage.dequeue

    if (suspended && stored < lowWatermark) {
      log.debug("resuming reading")
      connection ! ResumeReading
      suspended = false
    }

    if (storage.isEmpty) {
      if (closing) context stop self
      else context.unbecome()
    } else connection ! Write(storage(0), Ack)
  }

}
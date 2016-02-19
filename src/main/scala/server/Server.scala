package server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging}
import akka.io.{IO, Tcp}

class Server extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 0))

  def receive = {
    case b@Bound(localAddress) => log.info("Bounded...")

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      val connection = sender()
      log.info("received connection from {}", remote)
      val handler = context.actorOf(Handler.props(connection, remote))
      sender() ! Register(handler, keepOpenOnPeerClosed = true)
  }
}

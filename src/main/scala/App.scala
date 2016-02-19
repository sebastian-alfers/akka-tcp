import akka.actor.ActorSystem

object App extends App {
  implicit val actorSystem = ActorSystem("EchoServer")

  actorSystem.actorOf(TcpServer.props("0.0.0.0", 2020, EchoService.props))
}
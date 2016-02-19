import akka.actor.{Props, ActorSystem}
import server.Server

object App extends App {
  implicit val actorSystem = ActorSystem("EchoServer")

  actorSystem.actorOf(Props(new Server()))
}
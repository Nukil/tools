package akka.scala
/**
  * Created by Nukil on 2017/3/2.
  */
object Messages {

    trait Message extends Serializable

    case class Heartbeat(IP: String) extends Message

    case class RegisterWorker(IP: String) extends Message

    case object CheckWorkerTimeOut
}

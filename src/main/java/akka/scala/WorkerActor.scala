package akka.scala

import java.net.InetAddress
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorSelection}
import akka.pattern.Patterns
import akka.scala.Messages.{Heartbeat, RegisterWorker}
import akka.util.Timeout
import org.apache.log4j.Logger
import properties.LoadPropers

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global



/**
  * Created by Nukil on 2017/3/8.
  */
class WorkerActor(masterHost: String, masterPort: Int) extends Actor {
    val LOGGER: Logger = Logger.getLogger(this.getClass)
    var master: ActorSelection = _
    val workerIP: String = InetAddress.getLocalHost.getHostAddress
    //获取本机IP地址
    val HEART_INTERVAL: Int = LoadPropers.getProperties.getProperty("akka.heartbeat.interval", "10000").toInt

    override def preStart(): Unit = {
        registerWorkerToMaster()
    }

    override def receive: PartialFunction[Any, Unit] = {

        case Heartbeat(workerID) => {
            master ! Heartbeat(workerID)
        }
        case unexpected =>
            LOGGER.info(s"[Worker ERROR] ====== Worker [$workerIP] received the message can't parse!info is $unexpected ======")
    }

    private def registerWorkerToMaster(): Unit = {
        master = null
        while (null == master) {
            try {
                LOGGER.info(s"[Worker INFO] ====== The worker [$workerIP] has register to the master [$masterHost] ======")
                // "akka.tcp://remote-system@127.0.0.1:2552/user/remoteActor"
                master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
                val timeOut = new Timeout(Duration.create(10, "seconds"))
                val future = Patterns.ask(master, RegisterWorker(workerIP), timeOut)
                val result = Await.result(future, timeOut.duration)
                LOGGER.info(s"[Worker INFO] ====== ${result.toString}")

            } catch {
                case e: Exception =>
                    LOGGER.info(s"[Worker ERROR]====== Register has failed!sleep 10s and try again.info is ${e.getLocalizedMessage}")
                    TimeUnit.SECONDS.sleep(10)
                    master = null
            }
        }
        sendHeartBeatToMaster()
    }

    //启动检测服务
    private def sendHeartBeatToMaster(): Unit = {
        context.system.scheduler.schedule(0 millis, HEART_INTERVAL millis, self, Heartbeat(workerIP))
    }
}

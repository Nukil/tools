package akka.scala

import akka.actor.{Actor, ActorLogging}
import akka.scala.Messages._
import akka.scala.MasterServer.workerMap

import scala.concurrent.duration._
import scala.collection.JavaConversions._
import org.apache.log4j.Logger
import properties.LoadPropers

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Nukil on 2017/3/8.
  */
class MasterActor() extends Actor with ActorLogging {
    //超时时间
    val CHECK_INTERVAL: Int = LoadPropers.getProperties.getProperty("akka.check.interval", "30000").toInt
    val LOGGER: Logger = Logger.getLogger(this.getClass)

    override def receive: PartialFunction[Any, Unit] = {
        case RegisterWorker(workerIP) =>
            LOGGER.info(s"[Master INFO] ======New worker [$workerIP] connection ======")
            if (workerMap.containsKey(workerIP)) {
                workerMap -= workerIP
            }
            sender ! s"[Master INFO] ====== Worker [$workerIP] has Register Success! ======"

        case Heartbeat(workerIP) =>
            LOGGER.info(s"[Master INFO] ====== Master Received HB from worker [$workerIP] ====== $workerMap")
            if (workerMap.containsKey(workerIP)) {
                val oldWorker = workerMap(workerIP)
                oldWorker.heartbeat()
            } else {
                val newWorker = WorkerInfo()
                newWorker.actor = sender()
                newWorker.isAvailable = true
                workerMap.put(workerIP, newWorker)
                LOGGER.info(s"[Master INFO] ====== [$workerIP] join calculation ====== $workerMap")
            }

        case CheckWorkerTimeOut =>
            val currentTime = System.currentTimeMillis()
            val toRemove = workerMap.filter(x => (currentTime - x._2.lastHeartbeatTime) > CHECK_INTERVAL)
            for (rm <- toRemove) {
                LOGGER.info(s"[Master INFO] ====== Remove the death worker ====== $rm")
                workerMap -= rm._1
            }
        case unexpected =>
            LOGGER.error(s"[Master ERROR]====== $unexpected")
    }

    //定时检测超时数据
    override def preStart(): Unit = {
        context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckWorkerTimeOut)
    }
}
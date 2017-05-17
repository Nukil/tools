package akka.scala

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import properties.LoadPropers

/**
  * Created by Nukil on 2017/3/8.
  */
object MasterServer {
    val workerMap: ConcurrentHashMap[String, WorkerInfo] = new ConcurrentHashMap[String, WorkerInfo]()
    def startMaster(): Unit = {
        val masterHost: String = LoadPropers.getProperties.getProperty("akka.master.host", "127.0.0.1")
        val masterPort: Int = LoadPropers.getProperties.getProperty("akka.master.port", "30055").toInt
        val configStr =
            s"""
               |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
               |akka.remote.netty.tcp.hostname = "$masterHost"
               |akka.remote.netty.tcp.port = "$masterPort"
            """.stripMargin
        val config = ConfigFactory.parseString(configStr)
        //ActorSystem,辅助创建和监控下面的Actor,他是单例的
        val actorSystem = ActorSystem("MasterSystem", config)
        //创建Actor
        actorSystem.actorOf(Props(new MasterActor()), "Master")
    }
}

class MasterManager extends Thread {
    override def run(): Unit = {
        MasterServer.startMaster()
    }
}

case class WorkerInfo() {
    private var _lastHeartbeatTime: Long = _
    private var _isAvailable: Boolean = true
    private var _combatCapability: Long = 0L
    private var _actor: ActorRef = _

    def lastHeartbeatTime: Long = _lastHeartbeatTime
    def isAvailable: Boolean = _isAvailable
    def combatCapability: Long = _combatCapability
    def actor: ActorRef = _actor

    def lastHeartbeatTime_=(value: Long): Unit = {
        _lastHeartbeatTime = value
    }
    def isAvailable_=(value: Boolean): Unit = {
        _isAvailable = value
    }
    def combatCapability_=(value: Long): Unit = {
        _combatCapability = value
    }
    def actor_=(value: ActorRef): Unit = {
        _actor = value
    }

    def heartbeat(): Unit = {
        this.lastHeartbeatTime = System.currentTimeMillis()
    }
    def luckyWorker(): Unit = {
        this.isAvailable = false
        this.combatCapability = System.currentTimeMillis()
    }
    def releaseWorker(): Unit = {
        this.isAvailable = true
        this.combatCapability = System.currentTimeMillis()
    }
}
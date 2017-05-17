package akka.scala

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import properties.LoadPropers

/**
  * Created by Nukil on 2017/3/8.
  */
object WorkerServer extends App{
    workerStart()
    def workerStart(): Unit = {
        val masterHost: String = LoadPropers.getProperties.getProperty("akka.master.host", "127.0.0.1")
        val masterPort: Int = LoadPropers.getProperties.getProperty("akka.master.port", "30055").toInt

        // 准备配置
        val configStr =
            s"""
                |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
            """.stripMargin
        val config = ConfigFactory.parseString(configStr)
        //ActorSystem老大,辅助创建和监控下面的Actor,他是单例的
        val actorSystem = ActorSystem("WorkerSystem", config)
        //创建Actor,此时调用该(Actor)的prestart以及receive方法
        actorSystem.actorOf(Props(new WorkerActor(masterHost, masterPort)), "Worker")
    }
}

class WorkerManager extends Thread {
    override def run(): Unit = {
        WorkerServer.workerStart()
    }
}

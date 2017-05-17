package akka.scala

import org.apache.log4j.Logger


/**
  * Created by Nukil on 2017/3/8.
  */
object ClusterManager {
    val LOGGER: Logger = Logger.getLogger(this.getClass)

    def clusterStart(isMaster: Boolean): Unit = {
        if (isMaster) {
            val master = new MasterManager()
            master.start()
            LOGGER.info("master start")
        }
        val worker = new WorkerManager()
        worker.start()
        LOGGER.info("worker start")
    }
}

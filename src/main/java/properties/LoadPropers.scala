package properties

import java.util.Properties

import org.apache.log4j.Logger

/**
  * Created by Nukil on 2017/4/20.
  */

class LoadPropers {
    val logger: Logger = Logger.getLogger(this.getClass)
    def load(): Properties = {
        val classLoader = this.getClass.getClassLoader
        val properties: Properties = new Properties()
        properties.keySet()
        val serverConfigStream = classLoader.getResourceAsStream("server.properties")
        try {
            properties.load(serverConfigStream)
            properties
        } finally {
            try {
                serverConfigStream.close()
            } catch {
                case _: Throwable =>
            }
        }
    }
}
object LoadPropers {
    var properties: Properties = _
    def getProperties: Properties = {
        if (null == properties) {
            properties = new LoadPropers().load()
        }
        properties
    }
}


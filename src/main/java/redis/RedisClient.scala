package redis


import java.io.Serializable

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import properties.LoadPropers
import redis.clients.jedis.{Jedis, JedisPool}

/**
  * Created by Nukil on 2017/4/20.
  */
object RedisClient extends Serializable {
    val redisHost: String = LoadPropers.getProperties.getProperty("redis.host")
    val redisPort: Int = LoadPropers.getProperties.getProperty("redis.port").toInt
    val redisTimeout: Int = LoadPropers.getProperties.getProperty("redis.timeout").toInt

    var jedis: Jedis = getRedisPool.getResource

    private def getRedisPool: JedisPool = {
        new JedisPool(new GenericObjectPoolConfig(), redisHost, redisPort, redisTimeout)
    }

    def getJedis: Jedis = {
        if (null == jedis || jedis.isConnected) {
            jedis = getRedisPool.getResource
        }
        jedis
    }

    lazy val pool: JedisPool = getRedisPool
    lazy val hook = new Thread() {
        override def run(): Unit ={
            pool.destroy()
        }
    }
    sys.addShutdownHook(hook.run())
}

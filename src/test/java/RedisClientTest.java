import properties.LoadPropers;
import redis.RedisClient;
import redis.clients.jedis.Jedis;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nukil on 2017/4/20
 */
public class RedisClientTest {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(RedisClientTest.class);
        int redisIndex = Integer.parseInt(LoadPropers.getProperties().getProperty("redis.index"));

        Jedis jedis = RedisClient.getJedis();
        while (null == jedis || !jedis.isConnected()) {
            jedis = RedisClient.getJedis();
            if (null == jedis || !jedis.isConnected()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        jedis.select(redisIndex);
        Map<String, String> infoMap = new HashMap<String, String>();
        infoMap.put("startTime", "20170420");
        infoMap.put("endTime", "20170421");
        System.out.println(jedis.hmset("key", infoMap));
    }
}

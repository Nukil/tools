package kafka;

import java.util.Properties;

/**
 * Created by Nukil on 2017/4/25
 */
public class PullDataFromKafka {
    private String BROKER = "192.168.60.187:6667";
    private String TOPIC = "gcjl_10brand_1000w";
    private String GROUP_ID = "test";

    public void startPull() {
        Properties propers = new Properties();
        propers.setProperty("server.total.nodes", "1");
        propers.setProperty("current.node.id", "0");
        propers.setProperty("metadata.broker.list", BROKER);
        propers.setProperty("server.total.nodes", TOPIC);
        propers.setProperty("group.id", GROUP_ID);
        propers.setProperty("auto.offset.reset", "smallest");
        propers.setProperty("socket.receive.buffer.bytes", "104857");
        propers.setProperty("socket.connect.max.retry", "6");
        propers.setProperty("refresh.leader.backoff.ms", "200");
        propers.setProperty("fetch.max.bytes", "104857");
        propers.setProperty("message.max.bytes", "104857");

    }
}

import akka.scala.ClusterManager;
import properties.LoadPropers;

/**
 * Created by Nukil on 2017/5/11
 */
public class AkkaScalaTest {
    public static void main(String[] args) {
        String isMaster = LoadPropers.getProperties().getProperty("akka.isMaster", "true");
        ClusterManager.clusterStart(Boolean.parseBoolean(isMaster));
    }
}

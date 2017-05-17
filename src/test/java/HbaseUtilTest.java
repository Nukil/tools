import hbase.HBaseUtil;
import org.apache.hadoop.hbase.client.Result;

/**
 * Created by Nukil on 2017/4/27
 */
public class HbaseUtilTest {
    public static void main(String[] args) {
        Result result = HBaseUtil.getOneRow("car_feature_kafka_5000", "00001114910220152361440412719891045035");
        System.out.println(result);
    }
}

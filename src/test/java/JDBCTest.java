
import jdbc.JDBC;

import java.sql.ResultSet;

/**
 * Created by Nukil on 2017/4/25
 */
public class JDBCTest {
    public static void main(String[] args) {
        String driverName = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://192.168.60.187:3308/netposa";
        String userName = "root";
        String password = "Netposa123";
        JDBC jdbcTest = new JDBC(driverName, url, userName, password);
//        jdbc.JDBC jdbcTest = new jdbc.JDBC();
        ResultSet res = jdbcTest.executeQuery("select * from sjkk_gcjl", null);
        try {
            while (res.next()) {
                String a = res.getString(1);
                System.out.println(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

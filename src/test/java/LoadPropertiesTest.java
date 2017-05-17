import properties.LoadPropers;

import java.util.Properties;

/**
 * Created by Nukil on 2017/4/20
 */
public class LoadPropertiesTest {
    public static void main(String[] args) {
        Properties propers = LoadPropers.getProperties();
        System.out.println(propers.getProperty("master.host"));
        System.out.println(propers.getProperty("master.port"));
    }
}

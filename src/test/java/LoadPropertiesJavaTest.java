import properties.LoadPropersJava;

import java.util.Properties;

/**
 * Created by Nukil on 2017/4/20
 */
public class LoadPropertiesJavaTest {
    public static void main(String[] args) {
        LoadPropersJava instance = LoadPropersJava.getSingleInstance();
        Properties properties = instance.getProperties();
        System.out.println(properties.get("master.host"));
        System.out.println(properties.get("master.port"));
    }
}

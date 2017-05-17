import XMLUtils.ControlBean;

import java.io.InputStream;

/**
 * Created by Nukil on 2017/5/17
 */
public class ReadXMLTest {
    public static void main(String[] args) {
        ClassLoader classLoader = ReadXMLTest.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("control.xml");
        ControlBean controlConf = new ControlBean(inputStream);
        System.out.println(controlConf.toString());
    }
}

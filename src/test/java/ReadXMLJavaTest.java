import XMLUtils.ControlBeanJava;

/**
 * Created by Nukil on 2017/5/18
 */
public class ReadXMLJavaTest {
    public static void main(String[] args) {
        try {
            new ControlBeanJava("src/main/resources/control.xml");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}

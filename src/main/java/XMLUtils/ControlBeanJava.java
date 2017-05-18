package XMLUtils;


import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;

/**
 * Created by Nukil on 2017/5/18
 */
public class ControlBeanJava {
    public ControlBeanJava(String path) throws Exception {
        //创建SAXReader对象
        SAXReader reader = new SAXReader();
        //读取文件，转换成Document
        Document document = reader.read(new File(path));
        //获取根节点元素对象
        Element root = document.getRootElement();
        //遍历根节点
        listNodes(root);
    }

    private void listNodes(Element node) {
        System.out.println("root" + node.getName());
        List<Element> childElements = node.elements();
        for (Element element : childElements) {
            List<Attribute> attrs = element.attributes();
            for (Attribute attr : attrs) {
                System.out.println(attr.getName() + " " + attr.getValue());
            }
        }
    }
}


import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;


public class XMLParser {

    Element rootNode;


    public XMLParser(File xmlFile) throws IOException, JDOMException {

//        File[] files = Paths.get(new ClearingSystem().inputPath.toString())
//                .toFile()
//                .listFiles(f -> f.getName().contains("a"));

        SAXBuilder saxBuilder = new SAXBuilder();
        InputStreamReader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(xmlFile)), "Cp1251");
        org.jdom2.Document doc = saxBuilder.build(new InputSource(in));
        rootNode = doc.getRootElement();
    }

    public int getOrderCount() {
    return rootNode.getChildren("order").size();
    }

    public int getPositionCount(int orderNum){
//      в List индекс будет на 1 меньше
//        orderNum--;
        int positionCount = 0;
//        Находим путь к нужному чеку
        Element node = rootNode.getChildren("order").get(orderNum);
//        Находим все позиции найденного чека
        List<Element> positions = node.getChildren("positions").get(0).getChildren("position");
        positionCount = positions.size();
        return positionCount;
    }
// возвращает значение элемента определенной позиции в чеке
    public String getPositionElementValue(int orderNum, int positionNum, String field) {
//        orderNum--;
//        positionNum--;
        String value = "";
        Element node = rootNode.getChildren("order").get(orderNum);
//        Находим нужную позицию и формируем список всех ее элементов
        List<Element> elements = node.getChildren("positions").get(0).getChildren("position").get(positionNum).getChildren();
        for (Element element: elements){
            if (element.getName().equals(field)){
                value = element.getValue();
                return value;
            }
        }
        return "";
    }

    //возвращает значение из общих элементов в чеке
    public String getOrderElementValue(int orderNum, String field){
//        orderNum--;
        String value = "";
        Element node = rootNode.getChildren("order").get(orderNum);
        Element element = node.getChildren(field).get(0);
        return element.getValue();
    }

//    public boolean FieldPresents(int msgNumber, String field) {
//
//        List<Element> fieldsList = rootNode.getChildren("msg").get(msgNumber - 1).getChildren("field");
//        if (field.contains(".")) {
//            String f = field.substring(0, field.indexOf("."));
//            String sf = field.substring(field.indexOf(".") + 1);
//            for (Element el : fieldsList) {
//                if (el.getAttribute("id").getValue().equals(f)) {
//                    List<Element> subfieldList = el.getChildren("sub");
//                    for (Element el1 : subfieldList) {
//                        if (el1.getAttribute("id").getValue().equals(sf)) {
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//            }
//            return false;
//        } else {
//            for (Element el : fieldsList) {
//                if (el.getAttribute("id").getValue().equals(field)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//    }
//
//    public ArrayList<String> getMessageList() {
//        List<Element> messages = rootNode.getChildren("msg");
//        ArrayList<String> msgList = new ArrayList<>();
//        for (Element el : messages) {
//            msgList.add(el.getAttribute("mti").getValue());
//        }
//        return msgList;
//    }
//
}
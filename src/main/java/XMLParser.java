import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;
import java.io.*;
import java.util.List;


public class XMLParser {

    private Element rootNode;


    public XMLParser(File xmlFile) throws IOException, JDOMException {
        SAXBuilder saxBuilder = new SAXBuilder();
        InputStreamReader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(xmlFile)), "Cp1251");
        org.jdom2.Document doc = saxBuilder.build(new InputSource(in));
        rootNode = doc.getRootElement();
    }


    public int getOrderCount() {
    return rootNode.getChildren("order").size();
    }


    public int getPositionCount(int orderNum){
        int positionCount;
        Element node = rootNode.getChildren("order").get(orderNum);
        List<Element> positions = node.getChildren("positions").get(0).getChildren("position");
        positionCount = positions.size();
        return positionCount;
    }


    public String getPositionElementValue(int orderNum, int positionNum, String field) {
        String value;
        Element node = rootNode.getChildren("order").get(orderNum);
        List<Element> elements = node.getChildren("positions").get(0).getChildren("position")
                .get(positionNum).getChildren();
        for (Element element: elements){
            if (element.getName().equals(field)){
                value = element.getValue();
                return value;
            }
        }
        return "";
    }

    public String getOrderElementValue(int orderNum, String field) {
        Element node = rootNode.getChildren("order").get(orderNum);
        try {
            Element element = node.getChildren(field).get(0);
            return element.getValue();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    public boolean positionElementExists(int orderNum, int positionNum, String element){
        Element node = rootNode.getChildren("order").get(orderNum);
        List<Element> elements = node.getChildren("positions").get(0).getChildren("position")
                .get(positionNum).getChildren();
        return elements.stream()
                .anyMatch(f -> f.getName().equals(element));
    }

    public boolean orderElementExists(int orderNum, String element){
        Element node = rootNode.getChildren("order").get(orderNum);
        List<Element> elements = node.getChildren();
        return elements.stream()
                .anyMatch(f -> f.getName().equals(element));
    }
}
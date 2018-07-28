package processing;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;
import java.io.*;
import java.util.List;


/**
 * Класс для работы с XML - файлами с заказами
 */
public class XMLFile {

    private Element rootNode;


    /** Определяет корневой элемент, от которого будет производиться парсинг
     * @param xmlFile Файл для парсинга
     * @throws IOException
     * @throws JDOMException
     */
    public XMLFile(File xmlFile) throws IOException, JDOMException {
        SAXBuilder saxBuilder = new SAXBuilder();
        InputStreamReader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(xmlFile)), "Cp1251");
        org.jdom2.Document doc = saxBuilder.build(new InputSource(in));
        rootNode = doc.getRootElement();
    }


    /** Возвращает количество заказов в файле
     * @return Количество заказов
     */
    public int getOrderCount() {
    return rootNode.getChildren("order").size();
    }


    /** Возвращает количество позиций в заказе
     * @param orderNum Индекс заказа в файле
     * @return Количество позиций
     */
    public int getPositionCount(int orderNum){
        int positionCount;
        Element node = rootNode.getChildren("order").get(orderNum);
        List<Element> positions = node.getChildren("positions").get(0).getChildren("position");
        positionCount = positions.size();
        return positionCount;
    }


    /** Возвращает значение элемента позиции заказа
     * @param orderNum Индекс заказа в файле
     * @param positionNum Индекс позиции заказа в заказе
     * @param field Название элемента
     * @return Значение элемента
     */
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

    /** Возвращает значение элемента заказа
     * @param orderNum Индекс заказа в файле
     * @param field Название элемента
     * @return Значение элемента
     */
    public String getOrderElementValue(int orderNum, String field) {
        Element node = rootNode.getChildren("order").get(orderNum);
        try {
            Element element = node.getChildren(field).get(0);
            return element.getValue();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    /** Проверяет существование элемента позиции заказа
     * @param orderNum Индекс заказа в файле
     * @param positionNum Индекс позиции заказа в заказе
     * @param element Название элемента
     * @return true - элемент существует<br> false - элемент не существует
     */
    public boolean positionElementExists(int orderNum, int positionNum, String element){
        Element node = rootNode.getChildren("order").get(orderNum);
        List<Element> elements = node.getChildren("positions").get(0).getChildren("position")
                .get(positionNum).getChildren();
        return elements.stream()
                .anyMatch(f -> f.getName().equals(element));
    }

    /**  Проверяет существование элемента заказа
     * @param orderNum Индекс заказа в файле
     * @param element Название элемента
     * @return true - элемент существует<br> false - элемент не существует
     */
    public boolean orderElementExists(int orderNum, String element){
        Element node = rootNode.getChildren("order").get(orderNum);
        List<Element> elements = node.getChildren();
        return elements.stream()
                .anyMatch(f -> f.getName().equals(element));
    }
}
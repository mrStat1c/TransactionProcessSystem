import org.jdom2.JDOMException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Program {

    public static void main(String[] args) throws IOException, InterruptedException, JDOMException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Logger log = Logger.getLogger("main.log");

        OrderProcessingSystem cs = new OrderProcessingSystem();
        File[] files = null;
        Path inputPath = Paths.get(cs.systemProperties.getProperty("inputPath"));
        Path completedPath = Paths.get(cs.systemProperties.getProperty("completedPath"));
        Path failedPath = Paths.get(cs.systemProperties.getProperty("failedPath"));
        Path dublicatePath = Paths.get(cs.systemProperties.getProperty("dublicatePath"));
        IndicatorStamper indicatorStamper;

        try {
            files = inputPath.toFile()
                    .listFiles((dir, name) -> name.endsWith(".xml"));
        } catch (NullPointerException e) {
            log.info("Нет файлов для загрузки");
        }
        if (files != null) {
            MySQLDb db = new MySQLDb(cs.systemProperties);
            indicatorStamper = new IndicatorStamper(cs.systemProperties, db);
            for (File file : files) {
                try {
                    if (db.fileExists(file.getName())) {
                        db.createFile(file.getName(), OrderFileStatus.DUBLICATE);
                        Files.move(inputPath.resolve(file.getName()),
                                dublicatePath.resolve(file.getName()));
                    } else {
                        db.createFile(file.getName(), OrderFileStatus.PROCESSING);
                        XMLParser xmlFile = new XMLParser(file);
                        for (int i = 0; i < xmlFile.getOrderCount(); i++) {
                            List<OrderPosition> positions = new ArrayList<>();
                            for (int j = 0; j < xmlFile.getPositionCount(i); j++) {
                                //TODO придумать нормальный вариант добавления newProductInd в к-р OrderPosition
                                String newProductInd = "N";
                                if(xmlFile.positionElementExists(i,j,"newProductInd")){
                                    newProductInd = "Y";
                                }
                                positions.add(new OrderPosition(
                                        xmlFile.getPositionElementValue(i, j, "product"),
                                        xmlFile.getPositionElementValue(i, j, "price"),
                                        xmlFile.getPositionElementValue(i, j, "count"),
                                        xmlFile.getPositionElementValue(i, j, newProductInd)));
                            }
                            Order order = new Order(
                                    xmlFile.getOrderElementValue(i, "sale_point"),
                                    xmlFile.getOrderElementValue(i, "card"),
                                    xmlFile.getOrderElementValue(i, "date"),
                                    positions,
                                    xmlFile.getOrderElementValue(i, "currency"));
                            order = indicatorStamper.processOrder(order);
                            db.createOrder(order, file.getName());
                        }
                        db.updateFileStatus(file.getName(), OrderFileStatus.OK);
                        Files.move(inputPath.resolve(file.getName()),
                                completedPath.resolve(file.getName()));
                    }
                } catch (JDOMException e) {
                    System.out.println("Ошибка при обработке файла:\n" + e.getMessage());
                    db.updateFileStatus(file.getName(), OrderFileStatus.FAILED);
                    Files.move(inputPath.resolve(file.getName()),
                            failedPath.resolve(file.getName()));
                }
            }
        }


    }
}


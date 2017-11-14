import org.jdom2.JDOMException;
import java.io.File;
import java.io.FilenameFilter;
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

        try {
            files = inputPath.toFile()
                    .listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });
        } catch (NullPointerException e) {
            log.info("Нет файлов для загрузки");
        }
        if (files != null) {
            MySQLDb db = new MySQLDb(cs.systemProperties);
            for (File file : files) {
                try {
                    if (db.fileExists(file.getName())) {
                        db.createFile(file.getName(), "dublicate");
                    } else {
                        db.createFile(file.getName(), "processing");
                        XMLParser xmlFile = new XMLParser(file);
                        for (int i = 0; i < xmlFile.getOrderCount(); i++) {
                            List<OrderPosition> positions = new ArrayList<OrderPosition>();
                            for (int j = 0; j < xmlFile.getPositionCount(i); j++) {
                                positions.add(new OrderPosition(
                                        xmlFile.getPositionElementValue(i, j, "product"),
                                        xmlFile.getPositionElementValue(i, j, "price"),
                                        xmlFile.getPositionElementValue(i, j, "count")));
                            }
                            Order order = new Order(
                                    xmlFile.getOrderElementValue(i, "sale_point"),
                                    xmlFile.getOrderElementValue(i, "card"),
                                    xmlFile.getOrderElementValue(i, "date"),
                                    positions);
                            db.createOrder(order, file.getName());
                        }
                        db.updateFileStatus(file.getName(), "ok");
                        Files.move(inputPath.resolve(file.getName()),
                                completedPath.resolve(file.getName()));
                    }
                } catch (JDOMException e) {
                    System.out.println("Ошибка при обработке файла:\n" + e.getMessage());
                    db.updateFileStatus(file.getName(), "failed");
                    Files.move(inputPath.resolve(file.getName()),
                            failedPath.resolve(file.getName()));
                }
            }
        }


    }
}


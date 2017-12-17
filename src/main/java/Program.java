import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public class Program {

    public static void main(String[] args) throws IOException, InterruptedException, JDOMException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Logger log = Logger.getLogger("main.log");

        OrderProcessingSystem cs = new OrderProcessingSystem();
        List<File> files = new ArrayList<>();
        Path inputPath = Paths.get(cs.systemProperties.getProperty("inputPath"));
        Path completedPath = Paths.get(cs.systemProperties.getProperty("completedPath"));
        Path failedPath = Paths.get(cs.systemProperties.getProperty("failedPath"));
        Path dublicatePath = Paths.get(cs.systemProperties.getProperty("dublicatePath"));
        Path rejectedPath = Paths.get(cs.systemProperties.getProperty("rejectedPath"));
        IndicatorStamper indicatorStamper;
        MySQLDb db = new MySQLDb(cs.systemProperties);

        try {
            //TODO придумать, как сделать правильно проверки на пустые списки файлов в папке и файлов для загрузки
            files = new ArrayList<>(Arrays.asList(inputPath.toFile().listFiles()));
            for (int i = 0; i < files.size(); i++) {
                if (!OrderFileValidator.validateFile(files.get(i))) {
                    db.createFile(files.get(i).getName(), OrderFileStatus.REJECTED);
                 //   Files.move(inputPath.resolve(files.get(i).getName()),
                 //           rejectedPath.resolve(files.get(i).getName()));
                    files.remove(i);
                    i--;
                }
            }
        } catch (NullPointerException e) {
            log.info("Нет файлов для загрузки");
        }
        if (files.isEmpty()) {
            log.info("Нет файлов для загрузки");
            return;
        }

        indicatorStamper = new IndicatorStamper(cs.systemProperties, db);
        for (File file : files) {
            try {
                if (db.fileExists(file.getName())) {
                    db.createFile(file.getName(), OrderFileStatus.DUBLICATE);
                    //   Files.move(inputPath.resolve(file.getName()),
                    //          dublicatePath.resolve(file.getName()));
                } else {
                    db.createFile(file.getName(), OrderFileStatus.PROCESSING);
                    XMLParser xmlFile = new XMLParser(file);
                    for (int i = 0; i < xmlFile.getOrderCount(); i++) {
                        List<OrderPosition> positions = new ArrayList<>();
                        for (int j = 0; j < xmlFile.getPositionCount(i); j++) {
                            //TODO придумать нормальный вариант добавления newProductInd в к-р OrderPosition
                            String newProductInd = "N";
                            if (xmlFile.positionElementExists(i, j, "newProductInd")) {
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
                    //          Files.move(inputPath.resolve(file.getName()),
                    //                 completedPath.resolve(file.getName()));
                }
            } catch (JDOMException e) {
                System.out.println("Ошибка при обработке файла:\n" + e.getMessage());
                db.updateFileStatus(file.getName(), OrderFileStatus.FAILED);
                //     Files.move(inputPath.resolve(file.getName()),
                //             failedPath.resolve(file.getName()));
            }
        }
    }
}


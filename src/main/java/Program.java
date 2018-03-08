import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Program {

    public static void main(String[] args) throws IOException, InterruptedException, JDOMException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, ParseException {
         Logger log = LogManager.getLogger(Program.class.getName());

        List<File> files = new ArrayList<>();
        Path inputPath = Paths.get(systemProperties.get("inputPath"));
        Path completedPath = Paths.get(systemProperties.get("completedPath"));
        Path failedPath = Paths.get(systemProperties.get("failedPath"));
        Path dublicatePath = Paths.get(systemProperties.get("dublicatePath"));
        Path rejectedPath = Paths.get(systemProperties.get("rejectedPath"));
        IndicatorStamper indicatorStamper;
        MySQLDb db = new MySQLDb();

        try {
            log.info("File processing is starting.");
            //TODO придумать, как сделать правильно проверки на пустые списки файлов в папке и файлов для загрузки
            files = new ArrayList<>(Arrays.asList(inputPath.toFile().listFiles()));
            for (int i = 0; i < files.size(); i++) {
                if (!OrderFileValidator.validateFile(files.get(i))) {
                    db.createFile(files.get(i).getName(), OrderFileStatus.REJECTED);
                 //   Files.move(inputPath.resolve(files.get(i).getName()),
                 //           rejectedPath.resolve(files.get(i).getName()));
                    log.info("File " + files.get(i).getName() + " rejected.");
                    files.remove(i);
                    i--;
                }
            }
        } catch (NullPointerException e) {
            log.warn("No files for processing.");
        }
        if (files.isEmpty()) {
            log.warn("No files for processing.");
            return;
        }

        indicatorStamper = new IndicatorStamper(db);
        for (File file : files) {
            try {
                if (db.fileExists(file.getName())) {
                    db.createFile(file.getName(), OrderFileStatus.DUBLICATE);
                    //   Files.move(inputPath.resolve(file.getName()),
                    //          dublicatePath.resolve(file.getName()));
                    log.info("File " + file.getName() + " is dublicate.");
                } else {
                    db.createFile(file.getName(), OrderFileStatus.PROCESSING);
                    XMLParser xmlFile = new XMLParser(file);
                    for (int i = 0; i < xmlFile.getOrderCount(); i++) {
                        List<OrderPosition> positions = new ArrayList<>();
                        for (int j = 0; j < xmlFile.getPositionCount(i); j++) {
                            //TODO придумать нормальный вариант добавления newProductInd в к-р OrderPosition
                            boolean newProductInd = false;
                            if (xmlFile.positionElementExists(i, j, "newProductInd")) {
                                newProductInd = true;
                            }
                            positions.add(new OrderPosition(
                                    xmlFile.getPositionElementValue(i, j, "product"),
                                    xmlFile.getPositionElementValue(i, j, "price"),
                                    xmlFile.getPositionElementValue(i, j, "count"),
                                    newProductInd,
                                    j + 1));
                        }
                        Order order = new Order(
                                xmlFile.getOrderElementValue(i, "sale_point"),
//                                xmlFile.orderElementExists(i, "card") ?
//                                        xmlFile.getOrderElementValue(i, "card") : "",
                                xmlFile.getOrderElementValue(i, "card"),
                                xmlFile.getOrderElementValue(i, "date"),
                                positions,
                                xmlFile.getOrderElementValue(i, "currency"),
                                xmlFile.getOrderElementValue(i, "sale_point_order_num"));
                        if (OrderFileValidator.validateOrder(file.getName(), order)) {
                            order = indicatorStamper.processOrder(order);
                            db.createOrder(order, file.getName(), 'N');
                        } else {
                            db.createOrder(order, file.getName(), 'Y');
                            //Если реджект по sale_point, записать null
                        }
                    }
                    db.updateFileStatus(file.getName(), OrderFileStatus.OK);
                    //          Files.move(inputPath.resolve(file.getName()),
                    //                 completedPath.resolve(file.getName()));
                    log.info("File " + file.getName() + " processed.");
                }
            } catch (JDOMException e) {
                log.warn("e.getMessage()");
                db.updateFileStatus(file.getName(), OrderFileStatus.FAILED);
                //     Files.move(inputPath.resolve(file.getName()),
                //             failedPath.resolve(file.getName()));
                log.info("File " + file.getName() + " didn't process.");
            }
        }
        log.info("File processing finished.");
    }
}


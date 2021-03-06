package processing;

import orderModel.Order;
import orderModel.OrderPosition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * Класс, который содержит методы для управления системой
 */
public class SystemManager {

    private static Logger log = LogManager.getLogger(SystemManager.class.getName());
    private static MySQLDb db = new MySQLDb();

    private static Path inputPath = Paths.get(SystemProperties.get("inputPath"));
    private static Path completedPath = Paths.get(SystemProperties.get("completedPath"));
    private static Path failedPath = Paths.get(SystemProperties.get("failedPath"));
    private static Path dublicatePath = Paths.get(SystemProperties.get("dublicatePath"));
    private static Path rejectedPath = Paths.get(SystemProperties.get("rejectedPath"));
    private static IndicatorStamper indicatorStamper;

    /**
     * Возвращает список всех файлов, расположенных во входной директории
     *
     * @return список файлов
     */
    private static List<File> findFiles() {
        log.info("File search started.");
        File[] files = inputPath.toFile().listFiles();
        return files == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(files));
    }

    /**
     * Исключает файлы, отклоненные реджектами уровня File, и возвращает оставшиеся файлы
     *
     * @param files список файлов для валидации
     * @return список файлов после валидации
     */
    private static List<File> removeInvalidFiles(List<File> files) throws SQLException, IOException {
        for (int i = 0; i < files.size(); i++) {
            if (!OrderFileValidator.validateFile(files.get(i))) {
                db.createFile(files.get(i).getName(), OrderFileStatus.REJECTED);
//                    Files.move(inputPath.resolve(files.get(i).getName()), rejectedPath.resolve(files.get(i).getName()));
                log.info("File " + files.get(i).getName() + " rejected.");
                files.remove(i);
                i--;
            }
        }
        return files;
    }

    /**
     * Выполняет обработку файлов с заказами
     *
     */
    public static void startLoading() throws SQLException, IOException, ParseException {

        List<File> files = removeInvalidFiles(findFiles());
        if (files.isEmpty()) {
            log.warn("No files for processing.");
            return;
        }

        log.info("File processing started.");
        indicatorStamper = new IndicatorStamper(db);
        for (File file : files) {
            String fileName = file.getName();
            try {
                if (db.fileExists(fileName)) {
                    db.createFile(fileName, OrderFileStatus.DUBLICATE);
//                    Files.move(inputPath.resolve(fileName), dublicatePath.resolve(fileName));
                    log.info("File " + fileName + " is dublicate.");
                } else {
                    db.createFile(fileName, OrderFileStatus.PROCESSING);
                    processOrders(new XMLFile(file), fileName);
                    db.updateFileStatus(fileName, OrderFileStatus.OK);
//                    Files.move(inputPath.resolve(fileName), completedPath.resolve(fileName));
                    log.info("File " + fileName + " processed.");
                }
            } catch (JDOMException e) {
                log.warn("e.getMessage()");
                db.updateFileStatus(fileName, OrderFileStatus.FAILED);
//                Files.move(inputPath.resolve(fileName), failedPath.resolve(fileName));
                log.info("File " + fileName + " didn't process.");
            }
        }
        log.info("File processing finished.");
    }

    /**
     * Выполняет обработку заказов из xml файла
     *
     * @param xmlFile  файл с заказами типа processing.XMLFile
     * @param fileName имя файла
     */
    private static void processOrders(XMLFile xmlFile, String fileName) throws SQLException, ParseException {
        for (int i = 0; i < xmlFile.getOrderCount(); i++) {
            List<OrderPosition> positions = new ArrayList<>();
            for (int j = 0; j < xmlFile.getPositionCount(i); j++) {
                positions.add(new OrderPosition(
                        xmlFile.getPositionElementValue(i, j, "product"),
                        xmlFile.getPositionElementValue(i, j, "price"),
                        xmlFile.getPositionElementValue(i, j, "count"),
                        xmlFile.positionElementExists(i, j, "newProductInd"),
                        j + 1));
            }
            Order order = new Order(
                    xmlFile.getOrderElementValue(i, "sale_point"),
                    xmlFile.orderElementExists(i, "card") ? xmlFile.getOrderElementValue(i, "card") : "",
                    xmlFile.getOrderElementValue(i, "date"),
                    positions,
                    xmlFile.getOrderElementValue(i, "currency"),
                    xmlFile.getOrderElementValue(i, "sale_point_order_num"));
            if (OrderFileValidator.validateOrder(fileName, order)) {
                db.createOrder(order, fileName, 'N');
            } else {
                db.createOrder(order, fileName, 'Y');
            }
        }
    }

    /**
     * Формирует и выгружает отчеты
     */
    public static void startUnloading() throws IOException {
        ReportCreator.createReportSPTA();
        ReportCreator.createReportSPR();
    }

    public static void clean() throws SQLException {
        db.cleanProcessingData();
    }

    public static void sendDataToArchive() throws SQLException {
        db.sendDataToArchive();
    }
}

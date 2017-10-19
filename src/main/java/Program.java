


import org.jdom2.JDOMException;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.util.xml.jaxb.Table;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by Static on 17.07.2017.
 */
public class Program {

    public static void main(String[] args) throws IOException, InterruptedException, JDOMException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Logger log = Logger.getLogger("main.log");

        ClearingSystem cs = new ClearingSystem();
        File[] files = null;

        try {
            files = cs.inputPath.toFile().listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });
            //files = cs.inputPath.toFile().listFiles();
        } catch (NullPointerException e) {
            log.info("Нет файлов для загрузки");
        }
        if (files != null) {
            MySQLDb db = new MySQLDb();
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
//                System.out.println("orderCount = " + xmlFile.getOrderCount());
//                System.out.println("positionCount Order #1 = " + xmlFile.getPositionCount(1));
//                System.out.println("Order1.Position1. Product_id = " + xmlFile.getPositionElementValue(1, 1, "product_id"));
//                System.out.println("Order1.Position1. Product_id = " + xmlFile.getPositionElementValue(1, 2, "price"));
//                System.out.println("Order1.Position1. Product_id = " + xmlFile.getPositionElementValue(2, 1, "count"));
//                System.out.println("Order1.Position1. Product_id = " + xmlFile.getPositionElementValue(2, 2, "product_line"));
//                System.out.println("Order2. Date = " + xmlFile.getOrderElementValue(2,"date"));
                    }
                } catch (JDOMException e) {
                    System.out.println("Что-то пошло не так:\n" + e.getMessage());
                    db.updateFileStatus(file.getName(), "failed");
                }

//                Files.move(file.toPath(), cs.completedPath.resolve(file.getName()));
            }
        }


    }
}


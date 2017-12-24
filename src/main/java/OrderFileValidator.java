import com.sun.xml.internal.bind.v2.TODO;

import java.io.File;
import java.io.IOException;
import java.security.spec.ECField;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

class OrderFileValidator {

    private static OrderProcessingSystem cs;

    //TODO избавиться от static блока
    static {
        try {
            cs = new OrderProcessingSystem();
        } catch (IOException e) {
        }
    }

    private static MySQLDb db = new MySQLDb(cs.systemProperties);

    //Создают реджекты, если они есть и отдают true/false - прошла ли сущность проверку
    static boolean validateFile(File file) throws SQLException {
        if (file.getName().endsWith(".xml")) {
            return true;
        } else {
            db.createRejectForFile(file.getName(), 100);
            return false;
        }
    }

    static boolean validateOrder(String fileName, Order order) throws SQLException {
        return validateOrderDate(fileName, order)
                & validateLastPresentment(fileName, order);
    }

    private static boolean validateOrderDate(String fileName, Order order) throws SQLException {
        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
            try {
                calendar.setTime(dateFormat.parse(order.getDate()));
            } catch (ParseException e) {
                db.createRejectForOrder(fileName, order.getOrderNum(), 200, order.getDate());
                return false;
        }
        return true;
    }

    private static boolean validateLastPresentment(String fileName, Order order){
        //TODO заготовка
        return true;
    }

    static void validateOrderPosition(OrderPosition orderPosition) {

    }

}

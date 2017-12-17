import java.io.File;
import java.io.IOException;
import java.security.spec.ECField;
import java.sql.SQLException;
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

    static void validateOrder(Order order) {

    }

    static void validateOrderPosition(OrderPosition orderPosition) {

    }

}

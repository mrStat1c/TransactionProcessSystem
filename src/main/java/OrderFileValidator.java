
import com.sun.xml.internal.bind.v2.TODO;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    static boolean validateOrder(String fileName, Order order) throws SQLException, ParseException {
        if (!validateOrderFieldExisting(fileName, order) && !validateOrderForDublication(fileName, order)){
            return false;
        }
        boolean orderDateIsCorrect = validateOrderDate(fileName, order);
        boolean cardNumberIsCorrect = false;//карта существует и она корректна
        boolean salePointIsCorrect = validateSalePoint(fileName, order);
        //вроде дальше не верно, т.к. сначала нужно проверять корректность salePoint, а потом уже LateDispatch
        boolean result = (orderDateIsCorrect && validateLateDispatch(fileName, order)) & salePointIsCorrect;
        if (!order.getCard().isEmpty()) {
            cardNumberIsCorrect = validateCard(fileName, order);
            result &= cardNumberIsCorrect;
        }
        if (orderDateIsCorrect && cardNumberIsCorrect){
            result &= validateCardStatus(fileName, order);
        }
        result &= (validateCurrency(fileName, order) && salePointIsCorrect && validateForeignCurrency(fileName, order));
        return result;
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

    private static boolean validateLateDispatch(String fileName, Order order) throws ParseException, SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date orderDate = dateFormat.parse(order.getDate());
        double daysPassed = (new Date().getTime() - orderDate.getTime()) /
                (double) (1000 * 60 * 60 * 24); //86400000 секунд в одном дне
        if (daysPassed <= 3 || (db.lateDispatchAgreement(order) && daysPassed <= 7)) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 201, order.getDate());
            return false;
        }
    }

    private static boolean validateSalePoint(String fileName, Order order) throws SQLException {
        if (db.salePointExists(order.getSalePoint())) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 210, order.getSalePoint());
            return false;
        }
    }

    private static boolean validateCard(String fileName, Order order) throws SQLException {
        if (db.cardExists(order.getCard())) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 220, order.getCard());
            return false;
        }
    }

    private static boolean validateCardStatus(String fileName, Order order) throws SQLException, ParseException {
        String cardStatus = db.getCardStatusForOrderDate(order.getCard(), order.getDate());
        if (cardStatus.equals("Active")) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 221, cardStatus);
            return false;
        }
    }

    private static boolean validateCurrency(String fileName, Order order) throws SQLException {
        if (db.currencyExists(order.getCurrency())){
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 230, order.getCurrency());
            return false;
        }
    }


    private static boolean validateForeignCurrency(String fileName, Order order) throws SQLException {
        if (!order.getCurrency().equals("RUB")) {
            if (db.foreignCurrencyAgreement(order)) {
                return true;
            } else {
                db.createRejectForOrder(fileName, order.getOrderNum(), 231, order.getCurrency());
                return false;
            }
        } else {
            return true;
        }
    }

    private static boolean validateOrderFieldExisting(String fileName, Order order) throws SQLException {
        //TODO сделать через рефлексию
        //Field[] orderFields = order.getClass().getDeclaredFields();
        Map<String, String> orderFields = new HashMap<>();
        orderFields.put("currency", order.getCurrency());
        orderFields.put("date", order.getDate());
        orderFields.put("orderNum", String.valueOf(order.getOrderNum()));
        orderFields.put("salePoint", order.getSalePoint());
        orderFields.put("salePointOrderNum", order.getSalePointOrderNum());
        for(Map.Entry field: orderFields.entrySet()){
            if (field.getValue().equals("")){
                db.createRejectForOrder(fileName, order.getOrderNum(), 240, field.getKey() + " is absent");
                return false;
            }
        }
        if(order.getPositions().isEmpty()){
            db.createRejectForOrder(fileName, order.getOrderNum(), 240, "orderPositions are absent");
            return false;
        }
        return true;
    }

    private static boolean validateOrderForDublication(String fileName, Order order) throws SQLException {
        if(db.orderExists(order)){
            db.createRejectForOrder(fileName, order.getOrderNum(), 250, "dublicate");
            return false;
        } else {
            return true;
        }
    }

    static boolean validateOrderPosition(String fileName, int orderNum, String orderSalePoint, OrderPosition orderPosition) throws SQLException {
        if(!validateOrderPositionFieldExisting(fileName, orderNum, orderPosition)){
            return false;
        }
        boolean result = true;
        if (!orderPosition.getNewProductInd() || !db.newProductAgreement(orderSalePoint)){
                result = validateProduct(fileName, orderNum, orderPosition);
            }
        return result
                & validateProductCount(fileName, orderNum, orderPosition)
                & validateProductPrice(fileName, orderNum, orderPosition);
    }

    private static boolean validateProduct(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        if (db.productExists(orderPosition.getProduct())){
            return true;
        } else {
            db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 300, orderPosition.getProduct());
            return false;
        }
    }

    private static boolean validateProductCount(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        try {
            if (Integer.parseInt(orderPosition.getCount()) > 0
                    && orderPosition.getCount().length() <= 3) {
                return true;
            } else {
                db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 310, orderPosition.getCount());
                return false;
            }
        } catch (NumberFormatException e) {
            db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 310, orderPosition.getCount());
            return false;
        }
    }

    private static boolean validateProductPrice(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        try {
            if (Double.parseDouble(orderPosition.getCount()) > 0
                    && orderPosition.getPrice().substring(
                            orderPosition.getPrice().indexOf(".")).length() == 2) {
                return true;
            } else {
                db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 311, orderPosition.getPrice());
                return false;
            }
        } catch (NumberFormatException e) {
            db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 311, orderPosition.getPrice());
            return false;
        }
    }

    private static boolean validateOrderPositionFieldExisting(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        //TODO сделать через рефлексию
        //Field[] orderFields = order.getClass().getDeclaredFields();
        Map<String, String> orderPositionFields = new HashMap<>();
        orderPositionFields.put("product", orderPosition.getProduct());
        orderPositionFields.put("price", orderPosition.getPrice());
        orderPositionFields.put("count", orderPosition.getCount());
        for(Map.Entry field: orderPositionFields.entrySet()){
            if (field.getValue().equals("")){
                db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 320, field.getKey() + " is absent");
                return false;
            }
        }
        return true;
    }

}


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Выполняет валидацию файла с заказами на уровнях файла, заказов и позиций
 */
class OrderFileValidator {

    private static final Logger log = LogManager.getLogger(OrderFileValidator.class.getName());
    private static MySQLDb db = new MySQLDb();

    /** Производит валидацию файла и, в случае нахождения ошибки, создает запись об ошибке в бд
     * @param file Файл
     * @return true - файл прошел валидацию<br> false - файл не прошел валидацию
     * @throws SQLException
     */
    static boolean validateFile(File file) throws SQLException {
        if (file.getName().endsWith(".xml")) {
            return true;
        } else {
            db.createRejectForFile(file.getName(), 100);
            log.info("File " + file.getName() + " rejected with rejectCode 100");
            return false;
        }
    }

    /** Производит валидацию заказа по различным проверкам
     * @param fileName Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел валидацию<br> false - заказ не прошел валидацию
     * @throws SQLException
     * @throws ParseException
     */
    static boolean validateOrder(String fileName, Order order) throws SQLException, ParseException {
        if (!validateOrderFieldExisting(fileName, order) && !validateOrderForDublication(fileName, order)){
            return false;
        }
        boolean orderDateIsCorrect = validateOrderDate(fileName, order);
        boolean cardNumberIsCorrect = false;//карта существует и она корректна
        boolean salePointIsCorrect = validateSalePoint(fileName, order);
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

    /** Производит проверку корректности формата даты заказа, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateOrderDate(String fileName, Order order) throws SQLException {
        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        try {
            calendar.setTime(dateFormat.parse(order.getDate()));
        } catch (ParseException e) {
            db.createRejectForOrder(fileName, order.getOrderNum(), 200, order.getDate());
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 200");
            return false;
        }
        return true;
    }

    /** Производит проверку на своевременность отправки заказа в обработку, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateLateDispatch(String fileName, Order order) throws ParseException, SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date orderDate = dateFormat.parse(order.getDate());
        double daysPassed = (new Date().getTime() - orderDate.getTime()) /
                (double) (1000 * 60 * 60 * 24); //86400000 секунд в одном дне
        if (daysPassed <= 3 || (db.lateDispatchAgreement(order) && daysPassed <= 7)) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 201, order.getDate());
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 201");
            return false;
        }
    }

    /** Производит проверку на существование торговой точки, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateSalePoint(String fileName, Order order) throws SQLException {
        if (db.salePointExists(order.getSalePoint())) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 210, order.getSalePoint());
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 210");
            return false;
        }
    }

    /** Производит проверку на существование карты, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateCard(String fileName, Order order) throws SQLException {
        if (db.cardExists(order.getCard())) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 220, order.getCard());
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 220");
            return false;
        }
    }

    /** Производит проверку на валидность статуса карты на время создания заказа, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateCardStatus(String fileName, Order order) throws SQLException, ParseException {
        String cardStatus = db.getCardStatusForOrderDate(order.getCard(), order.getDate());
        if (cardStatus.equals("Active")) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 221, cardStatus);
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 221");
            return false;
        }
    }

    /** Производит проверку на существование кода валюты, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateCurrency(String fileName, Order order) throws SQLException {
        if (db.currencyExists(order.getCurrency())){
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 230, order.getCurrency());
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 230");
            return false;
        }
    }

    /** Производит проверку на разрешение обработки конкретной валюты для торговой точки, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateForeignCurrency(String fileName, Order order) throws SQLException {
        if (!order.getCurrency().equals("RUB")) {
            if (db.foreignCurrencyAgreement(order)) {
                return true;
            } else {
                db.createRejectForOrder(fileName, order.getOrderNum(), 231, order.getCurrency());
                log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 231");
                return false;
            }
        } else {
            return true;
        }
    }


    /** Производит проверку на наличие обязательных полей в заказе, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
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
                log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 240");
                return false;
            }
        }
        if(order.getPositions().isEmpty()){
            db.createRejectForOrder(fileName, order.getOrderNum(), 240, "orderPositions are absent");
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 240");
            return false;
        }
        return true;
    }

    /** Производит проверку заказа на дубликат, и в случае нахождения дубликата, создает запись об ошибке в бд
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateOrderForDublication(String fileName, Order order) throws SQLException {
        if(db.orderExists(order)){
            db.createRejectForOrder(fileName, order.getOrderNum(), 250, "dublicate");
            log.info("File " + fileName + ". Order " + order.getOrderNum() + " rejected with rejectCode 250");
            return false;
        } else {
            return true;
        }
    }

    /** Производит валидацию позиции заказа по различным проверкам
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNum Системный номер заказа
     * @param orderSalePoint Название торговой точки в заказе
     * @param orderPosition Позиция заказа
     * @return true - позиция заказа прошла валидацию<br> false - позиция заказа не прошла валидацию
     * @throws SQLException
     */
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


    /** Производит проверку на существование продукта, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNum Системный номер заказа
     * @param orderPosition Позиция заказа
     * @return  позиция заказа прошла проверку<br> false - позиция заказа не прошла проверку
     * @throws SQLException
     */
    private static boolean validateProduct(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        if (db.productExists(orderPosition.getProduct())){
            return true;
        } else {
            db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 300, orderPosition.getProduct());
            log.info("File " + fileName + ". Order " + orderNum + ". OrderPosition " + orderPosition.getNumber() + " rejected with rejectCode 300");
            return false;
        }
    }

    /** Производит проверку на корректность количества продукта, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNum Системный номер заказа
     * @param orderPosition Позиция заказа
     * @return  позиция заказа прошла проверку<br> false - позиция заказа не прошла проверку
     * @throws SQLException
     */
    private static boolean validateProductCount(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        try {
            if (Integer.parseInt(orderPosition.getCount()) > 0
                    && orderPosition.getCount().length() <= 3) {
                return true;
            } else {
                db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 310, orderPosition.getCount());
                log.info("File " + fileName + ". Order " + orderNum + ". OrderPosition " + orderPosition.getNumber() + " rejected with rejectCode 310");
                return false;
            }
        } catch (NumberFormatException e) {
            db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 310, orderPosition.getCount());
            log.info("File " + fileName + ". Order " + orderNum + ". OrderPosition " + orderPosition.getNumber() + " rejected with rejectCode 310");
            return false;
        }
    }

    /** Производит проверку на корректность цены продукта, и в случае нахождения ошибки, создает запись об ошибке в бд
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNum Системный номер заказа
     * @param orderPosition Позиция заказа
     * @return  позиция заказа прошла проверку<br> false - позиция заказа не прошла проверку
     * @throws SQLException
     */
    private static boolean validateProductPrice(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        try {
            if (Double.parseDouble(orderPosition.getCount()) > 0
                    && orderPosition.getPrice().substring(
                            orderPosition.getPrice().indexOf(".")).length() == 2) {
                return true;
            } else {
                db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 311, orderPosition.getPrice());
                log.info("File " + fileName + ". Order " + orderNum + ". OrderPosition " + orderPosition.getNumber() + " rejected with rejectCode 311");
                return false;
            }
        } catch (NumberFormatException e) {
            db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 311, orderPosition.getPrice());
            log.info("File " + fileName + ". Order " + orderNum + ". OrderPosition " + orderPosition.getNumber() + " rejected with rejectCode 311");
            return false;
        }
    }

    /** Производит проверку на наличие обязательных полей в позиции заказа, и в случае отсутствия поля, создает запись об ошибке в бд
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNum Системный номер заказа
     * @param orderPosition Позиция заказа
     * @return  позиция заказа прошла проверку<br> false - позиция заказа не прошла проверку
     * @throws SQLException
     */
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
                log.info("File " + fileName + ". Order " + orderNum + ". OrderPosition " + orderPosition.getNumber() + " rejected with rejectCode 320");
                return false;
            }
        }
        return true;
    }

}

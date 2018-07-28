package processing;

import model.Order;
import model.OrderPosition;
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

    /**
     * Производит валидацию файла и, в случае нахождения ошибки, создает запись об ошибке в бд
     *
     * @param file Файл
     * @return true - файл прошел валидацию<br> false - файл не прошел валидацию
     * @throws SQLException
     */
    static boolean validateFile(File file) throws SQLException {
        if (file.getName().endsWith(".xml")) {
            return true;
        } else {
            db.createRejectForFile(file.getName(), 100);
            return false;
        }
    }

    /**
     * Производит валидацию заказа по различным проверкам
     *
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
        boolean cardNumberIsCorrect = false;
        boolean salePointIsCorrect = validateSalePoint(fileName, order);
        boolean result = (orderDateIsCorrect && validateLateDispatch(fileName, order)) & salePointIsCorrect;
        if (!order.getCard().isEmpty()) {
            cardNumberIsCorrect = validateCard(fileName, order);
            result &= cardNumberIsCorrect;
        }
        if (orderDateIsCorrect && cardNumberIsCorrect){
            result &= validateCardStatus(fileName, order);
        }
        result &= (validateCurrency(fileName, order) && validateForeignCurrency(fileName, order));
        return result;
    }

    /**
     *  Производит проверку корректности формата даты заказа, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
            return false;
        }
        return true;
    }

    /**
     *  Производит проверку на своевременность отправки заказа в обработку, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateLateDispatch(String fileName, Order order) throws ParseException, SQLException {
        double secondCountInOneDay = 86400000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date orderDate = dateFormat.parse(order.getDate());
        double daysPassed = (new Date().getTime() - orderDate.getTime()) / secondCountInOneDay;
        if (daysPassed <= 3 || (db.lateDispatchAgreement(order) && daysPassed <= 7)) {
            return true;
        } else {
            db.createRejectForOrder(fileName, order.getOrderNum(), 201, order.getDate());
            return false;
        }
    }

    /**
     * Производит проверку на существование торговой точки, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
            return false;
        }
    }

    /**
     * Производит проверку на существование карты, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
            return false;
        }
    }

    /**
     * Производит проверку на валидность статуса карты на время создания заказа, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
            return false;
        }
    }

    /**
     *  Производит проверку на существование кода валюты, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
            return false;
        }
    }

    /**
     * Производит проверку на разрешение обработки конкретной валюты для торговой точки, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     *  Производит проверку на наличие обязательных полей в заказе, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateOrderFieldExisting(String fileName, Order order) throws SQLException {
        Map<String, String> requiredOrderFields = new HashMap<>();
        requiredOrderFields.put("currency", order.getCurrency());
        requiredOrderFields.put("date", order.getDate());
        requiredOrderFields.put("orderNum", String.valueOf(order.getOrderNum()));
        requiredOrderFields.put("salePoint", order.getSalePoint());
        requiredOrderFields.put("salePointOrderNum", order.getSalePointOrderNum());

        for(Map.Entry field: requiredOrderFields.entrySet()){
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

    /**
     * Производит проверку заказа на дубликат, и в случае нахождения дубликата, создает запись об ошибке в бд
     *
     * @param fileName  Имя файла, в котором находится заказ
     * @param order Заказ
     * @return true - заказ прошел проверку<br> false - заказ не прошел проверку
     * @throws SQLException
     */
    private static boolean validateOrderForDublication(String fileName, Order order) throws SQLException {
        if(db.orderExists(order)){
            db.createRejectForOrder(fileName, order.getOrderNum(), 250, "dublicate");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Производит валидацию позиции заказа по различным проверкам
     *
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


    /**
     * Производит проверку на существование продукта, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
            return false;
        }
    }

    /**
     * Производит проверку на корректность количества продукта, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
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
                return false;
            }
        } catch (NumberFormatException e) {
            db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 310, orderPosition.getCount());
            return false;
        }
    }

    /**
     * Производит проверку на корректность цены продукта, и в случае нахождения ошибки, создает запись об ошибке в бд
     *
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNum Системный номер заказа
     * @param orderPosition Позиция заказа
     * @return  позиция заказа прошла проверку<br> false - позиция заказа не прошла проверку
     * @throws SQLException
     */
    private static boolean validateProductPrice(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        try {
            if (Double.parseDouble(orderPosition.getPrice()) > 0
                    && orderPosition.getPrice().substring(
                            orderPosition.getPrice().indexOf(".") + 1).length() == 2) {
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

    /**
     *  Производит проверку на наличие обязательных полей в позиции заказа, и в случае отсутствия поля, создает запись об ошибке в бд
     *
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNum Системный номер заказа
     * @param orderPosition Позиция заказа
     * @return  позиция заказа прошла проверку<br> false - позиция заказа не прошла проверку
     * @throws SQLException
     */
    private static boolean validateOrderPositionFieldExisting(String fileName, int orderNum, OrderPosition orderPosition) throws SQLException {
        Map<String, String> requiredOrderPositionFields = new HashMap<>();
        requiredOrderPositionFields.put("product", orderPosition.getProduct());
        requiredOrderPositionFields.put("price", orderPosition.getPrice());
        requiredOrderPositionFields.put("count", orderPosition.getCount());

        for(Map.Entry field: requiredOrderPositionFields.entrySet()){
            if (field.getValue().equals("")){
                db.createRejectForOrderPosition(fileName, orderNum, orderPosition.getNumber(), 320, field.getKey() + " is absent");
                return false;
            }
        }
        return true;
    }

}

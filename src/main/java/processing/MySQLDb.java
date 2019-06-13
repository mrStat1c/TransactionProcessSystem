package processing;

import orderModel.Order;
import orderModel.OrderPosition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Класс для работы с данными в бд Mysql
 */
public class MySQLDb {

    private static Logger log = LogManager.getLogger(MySQLDb.class.getName());
//    private Statement statement = null;
    private Connection connection;
    private PreparedStatement statement = null;
    private ResultSet resultSet;

    /**
     * Создание подключения к бд
     */
    public MySQLDb() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s/hw10?user=%s&password=%s",
                            SystemProperties.get("db.dictionaries.server"),
                            SystemProperties.get("db.dictionaries.user"),
                            SystemProperties.get("db.dictionaries.password")));
        } catch (Exception e) {
            System.out.println("Ошибка при подключении к бд:\n" + e.getMessage());
        }
    }

    private void getResultSet(String query) throws SQLException {
        resultSet = statement.executeQuery(query);
        resultSet.next();
    }


    /**
     * Возвращает идентификатор торговой точки по ее названию
     *
     * @param salePointName Название торговой точки
     * @return Идентификатор торговой точки
     * @throws SQLException
     */
    public int getSalePointId(String salePointName) throws SQLException {
        String query = "SELECT id FROM dictionaries.sale_points WHERE name = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, salePointName);
        getResultSet(query);
        try {
            return resultSet.getInt("id");
        } catch (SQLException e) {
            //TODO придумать нормальную реализацию
            return 0;
        }
    }

    /**
     * Возвращает идентификатор карты по ее номеру
     *
     * @param cardNumber Номер карты
     * @return Идентификатор карты
     * @throws SQLException
     */
    public int getCardId(String cardNumber) throws SQLException {
        String query = "SELECT id FROM dictionaries.cards WHERE number = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, cardNumber);
        getResultSet(query);
        try {
            return resultSet.getInt("id");
        } catch (SQLException e) {
            //TODO придумать нормальные реализации (тег card отсутствует и карта не найдена)
            return 0;
        }
    }

    
    /**
     * Возвращает идентификатор продукта по его названию
     *
     * @param productName Название продукта
     * @return Идентификатор продукта
     */
    public int getProductId(String productName) throws SQLException {
        String query = "SELECT id FROM dictionaries.products WHERE name = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, productName);
        try {
            getResultSet(query);
            return resultSet.getInt("id");
        } catch (SQLException e){
            return 0;
        }
    }

    /**
     * Возвращает название продуктовой линии продукта по названию продукта
     *
     * @param productName Название продукта
     * @return Название продуктовой линии
     * @throws SQLException
     */
    public String getProductLine(String productName) throws SQLException {
        String query = "SELECT name FROM dictionaries.product_lines WHERE id = " +
                "(SELECT product_line_id FROM dictionaries.products WHERE name = ?)";
        statement = connection.prepareStatement(query);
        statement.setString(1, productName);
        getResultSet(query);
        return resultSet.getString("name");
    }

    /**
     * Возвращает идентификатор файла по его имени
     *
     * @param fileName Имя Файла
     * @return Идентификатор файла
     * @throws SQLException
     */
    public int getFileId(String fileName) throws SQLException {
        String query = "SELECT id FROM processing.files WHERE name = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, fileName);
        getResultSet(query);
        return resultSet.getInt("id");
    }


    /**
     * Проверяет, существует ли файл в бд по его имени
     *
     * @param fileName Имя файла
     * @return true - файл существует<br> false - файл не существует
     * @throws SQLException
     */
    public boolean fileExists(String fileName) throws SQLException {
        String query = "SELECT count(*) as fileCount from processing.files WHERE name = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, fileName);
        getResultSet(query);
        return resultSet.getInt("fileCount") != 0;
    }

    /**
     * Создает запись о файле в бд
     *
     * @param fileName Имя файла
     * @param status   Статус файла
     * @throws SQLException
     */
    public void createFile(String fileName, OrderFileStatus status) throws SQLException {
        String query = "INSERT INTO processing.files (name, status) VALUES ( ?, ?);";
        statement = connection.prepareStatement(query);
        statement.setString(1, fileName);
        statement.setString(2, status.toString());
        statement.execute(query);
    }

    /**
     * Обновляет статус файла в бд
     *
     * @param fileName Имя файла
     * @param status   Новый статус файла
     * @throws SQLException
     */
    public void updateFileStatus(String fileName, OrderFileStatus status) throws SQLException {
        String query = "UPDATE processing.files SET status = ? WHERE name = ?;";
        statement = connection.prepareStatement(query);
        statement.setString(1, fileName);
        statement.setString(2, status.toString());
        statement.execute(query);
    }

    /**
     * Создает запись о заказе в бд
     *
     * @param order    Заказ
     * @param fileName Имя файла, в котором находится заказ
     * @param rejected true - Заказ отклонен, false - Заказ обработан без ошибок
     * @throws SQLException
     */
    public void createOrder(Order order, String fileName, char rejected) throws SQLException {
        String query;
        int orderNumber = order.getOrderNum();
        double orderSum = 0.0;
        List<OrderPosition> validedOrderPositions = new ArrayList<>();
        if (String.valueOf(rejected).equals("N")) {
            for (OrderPosition position : order.getPositions()) {
                if (OrderFileValidator.validateOrderPosition(fileName, order.getOrderNum(), order.getSalePoint(), position)) {
                    createOrderPosition(position, orderNumber, order.getCurrency(), order.getDate(), 'N');
                    validedOrderPositions.add(position);
                } else {
                    createOrderPosition(position, orderNumber, order.getCurrency(), order.getDate(), 'Y');
                }
            }
            query = "SELECT SUM(settl_price) AS order_sum FROM processing.order_positions WHERE rejected = 'N' and order_number =?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, orderNumber);
            getResultSet(query);
            orderSum = resultSet.getDouble("order_sum");
        }
        query = "INSERT INTO processing.orders (number, sale_point_id, order_date, card_id, " +
                "file_id, ccy_id, sum, rejected, sale_point_order_num) VALUES (?,?,?,?,?,?,?,?,?);";
        statement = connection.prepareStatement(query);
        statement.setInt(1, orderNumber);
        statement.setInt(2, getSalePointId(order.getSalePoint()));
        statement.setString(3, "DATE_FORMAT('" + order.getDate() + "', '%Y-%m-%d %H:%i:%s')");
        statement.setInt(4, getCardId(order.getCard()));
        statement.setInt(5, getFileId(fileName));
        statement.setInt(6, getCurrencyId(order.getCurrency()));
        statement.setDouble(7, orderSum);
        statement.setString(8, String.valueOf(rejected));
        statement.setString(9, order.getSalePointOrderNum());
        statement.execute(query);
//        TODO пересмотреть логику (убрать индикаторы из класса Order и создавать индикаторы только на основе данных в бд этого чека)
        if (!validedOrderPositions.isEmpty()) {
            Order order1 = new Order(
                    order.getSalePoint(),
                    order.getCard(),
                    order.getDate(),
                    validedOrderPositions,
                    order.getCurrency(),
                    order.getSalePointOrderNum()
            );
            order1 = new IndicatorStamper(this).processOrder(order1);
            if (!order1.getIndicators().isEmpty()) {
                createOrderIndicators(order1.getIndicators(), orderNumber);
            }
        }

    }


    /**
     * Проверяет, существует ли заказ в бд
     *
     * @param order Заказ
     * @return true - Заказ существует в бд<br> false - Заказ не существует в бд
     * @throws SQLException
     */
    boolean orderExists(Order order) throws SQLException {
        String query = "SELECT count(*) count FROM processing.orders WHERE order_date = ? AND sale_point_id = ?" +
               " AND sale_point_order_num = ?;";
        statement = connection.prepareStatement(query);
        statement.setString(1, order.getDate());
        statement.setInt(2, getSalePointId(order.getSalePoint()));
        statement.setString(3, order.getSalePointOrderNum());
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Возвращает идентификатор курса валюты по коду валюты
     *
     * @param currencyСode Код валюты
     * @return Идентификатор валюты
     * @throws SQLException
     */
    private int getCurrencyId(String currencyСode) throws SQLException {
        String query = "SELECT id FROM dictionaries.currencies WHERE code = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, currencyСode);
        getResultSet(query);
        try {
            return resultSet.getInt("id");
        } catch (SQLException e) {
            //TODO придумать нормальную реализацию
            return 0;
        }
    }

    /**
     * Создает запись в бд о позиции в заказе
     *
     * @param orderPosition Позиция в заказе
     * @param orderId       Идентификатор заказа в бд
     * @param currencyCode  Код валюты заказа
     * @param orderDate     Дата заказа
     * @param rejected      true - Позиция заказа отклонена, false - Позиция заказа обработана без ошибок
     * @throws SQLException
     */
    private void createOrderPosition(OrderPosition orderPosition, int orderId, String currencyCode, String orderDate, char rejected) throws SQLException {
        Double settlPrice = getCurrencyCourse(currencyCode, orderDate) * Double.parseDouble(orderPosition.getPrice());
        String query = "INSERT INTO processing.order_positions (order_number, product_id, orig_price, settl_price, count, rejected)" +
                " VALUES (?,?,?,?,?,?)";
        statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        statement.setInt(2, getProductId(orderPosition.getProduct()));
        statement.setString(3, orderPosition.getPrice());
        statement.setDouble(4, settlPrice);
        statement.setString(5, orderPosition.getCount());
        statement.setString(6, String.valueOf(rejected));
        statement.execute(query);
    }


    /**
     * Возвращает курс валюты на конкретную дату
     *
     * @param currencyCode Код валюты
     * @param courseDate   Дата, на которую необходимо определить курс
     * @return Курс валюты
     * @throws SQLException
     */
    public Double getCurrencyCourse(String currencyCode, String courseDate) throws SQLException {
        String query = "SELECT course FROM dictionaries.currency_courses WHERE ccy_id = ? AND date = ?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, getCurrencyId(currencyCode));
        statement.setString(2, courseDate.substring(0, 10));
        getResultSet(query);
        return resultSet.getDouble("course");
    }

    /**
     * Создает записи в бд обо всех индикаторах заказа
     *
     * @param orderIndicators Индикаторы заказа
     * @param orderNumber         Идентификатор заказа в бд
     * @throws SQLException
     */
    private void createOrderIndicators(Set<String> orderIndicators, int orderNumber) throws SQLException {
        orderIndicators.forEach(indicator ->
        {
            String query = "INSERT INTO processing.order_indicators (order_number, indicator) VALUES (?,?);";
            try {
                statement = connection.prepareStatement(query);
                statement.setInt(1, orderNumber);
                statement.setString(2, indicator);
                statement.execute(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Создает запись об отклонении файла от обработки
     *
     * @param fileName   Имя файла
     * @param rejectCode Код ошибки из-за которой файл отклоняется от обработки
     * @throws SQLException
     */
    public void createRejectForFile(String fileName, int rejectCode) throws SQLException {
        String query = "INSERT INTO processing.rejects (file_name, order_number, order_position_number, " +
                "type, code) VALUES (?,?,?,?,?);";
        statement = connection.prepareStatement(query);
        statement.setString(1, fileName);
        statement.setString(2, null);
        statement.setString(3, null);
        statement.setString(4, RejectType.FILE.toString());
        statement.setInt(5, rejectCode);
        statement.execute(query);
        log.info("File " + fileName + " rejected with rejectCode " + rejectCode);
    }


    /**
     * Создает запись об отклонении заказа от обработки
     *
     * @param fileName    Имя файла, в котором находится заказ
     * @param orderNumber Системный номер заказа
     * @param rejectCode  Код ошибки из-за которой заказ отклоняется от обработки
     * @param fieldValue  Значение поля из-за которого произошла ошибка
     * @throws SQLException
     */
    public void createRejectForOrder(String fileName, int orderNumber, int rejectCode, String fieldValue) throws SQLException {
        String query = "INSERT INTO processing.rejects (file_name, order_number, order_position_number, " +
                "type, code, incorrect_field_value) VALUES (?,?,?,?,?,?);";
        statement = connection.prepareStatement(query);
        statement.setString(1, fileName);
        statement.setInt(2, orderNumber);
        statement.setString(3, null);
        statement.setString(4, RejectType.ORDER.toString());
        statement.setInt(5, rejectCode);
        statement.setString(6, fieldValue);
        statement.execute(query);
        log.info("File " + fileName + ". Order " + orderNumber + " rejected with rejectCode " + rejectCode);
    }

    /**
     * Создает запись об отклонении позиции заказа от обработки
     *
     * @param fileName            Имя файла, в котором находится заказ
     * @param orderNumber         Системный номер заказа, в котором находится позиция заказа
     * @param orderPositionNumber Порядковый номер позиции в заказе
     * @param rejectCode          Код ошибки из-за которой позиция заказа отклоняется от обработки
     * @param fieldValue          Значение поля из-за которого произошла ошибка
     * @throws SQLException
     */
    public void createRejectForOrderPosition(String fileName, int orderNumber, int orderPositionNumber, int rejectCode,
                                             String fieldValue) throws SQLException {
        String query = "INSERT INTO processing.rejects (file_name, order_number, order_position_number, " +
                "type, code, incorrect_field_value) VALUES (?,?,?,?,?,?);";
        statement = connection.prepareStatement(query);
        statement.setString(1, fileName);
        statement.setInt(2, orderNumber);
        statement.setInt(3, orderPositionNumber);
        statement.setString(4, RejectType.ORDER_POSITION.toString());
        statement.setInt(5, rejectCode);
        statement.setString(6, fieldValue);
                statement.execute(query);
        log.info("File " + fileName + ". Order " + orderNumber + ". OrderPosition " + orderPositionNumber
                + " rejected with rejectCode " + rejectCode);
    }

    /**
     * Проверяет наличие соглашения у торговой точки на позднее представление заказов на обработку
     *
     * @param order Заказ, в рамках которого происходит проверка
     * @return true - соглашение имеется<br> false - соглашение отсутствует
     * @throws SQLException
     */
    public boolean lateDispatchAgreement(Order order) throws SQLException {
        String query = "SELECT count(*) as count from dictionaries.sale_point_agreements" +
                " WHERE type = 'LateDispatch'" +
                " AND sale_point_id =" +
                " (SELECT id FROM dictionaries.sale_points WHERE name = ?);";
        statement = connection.prepareStatement(query);
        statement.setString(1, order.getSalePoint());
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Проверяет наличие соглашения у торговой точки на представление заказов с иностранной валютой на обработку
     *
     * @param order Заказ, в рамках которого происходит проверка
     * @return true - соглашение имеется<br> false - соглашение отсутствует
     * @throws SQLException
     */
    public boolean foreignCurrencyAgreement(Order order) throws SQLException {
        String query = "SELECT count(*) as count from dictionaries.sale_point_agreements" +
                " WHERE type = 'ForeignCurrency'" +
                " AND sale_point_id =" +
                " (SELECT id FROM dictionaries.sale_points WHERE name = ?);";
        statement = connection.prepareStatement(query);
        statement.setString(1, order.getSalePoint());
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Проверяет наличие соглашения у торговой точки на представление заказов с неизвестным продуктом на обработку
     *
     * @param salePoint Название торговой точки
     * @return true - соглашение имеется<br> false - соглашение отсутствует
     * @throws SQLException
     */
    public boolean newProductAgreement(String salePoint) throws SQLException {
        String query = "SELECT count(*) as count from dictionaries.sale_point_agreements" +
                " WHERE type = 'NewProduct'" +
                " AND sale_point_id =" +
                " (SELECT id FROM dictionaries.sale_points WHERE name = ?);";
        statement = connection.prepareStatement(query);
        statement.setString(1, salePoint);
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Проверяет существование в бд торговой точки
     *
     * @param salePoint Название торговой точки
     * @return true - торговая точка существует<br> false - торговая точка не существует
     * @throws SQLException
     */
    public boolean salePointExists(String salePoint) throws SQLException {
        String query = "SELECT count(*) as count from dictionaries.sale_points" +
                " WHERE name = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, salePoint);
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Проверяет существование в бд карты
     *
     * @param cardNumber Номер карты
     * @return true - карта существует<br> false - карта не существует
     * @throws SQLException
     */
    public boolean cardExists(String cardNumber) throws SQLException {
        String query = "SELECT count(*) as count from dictionaries.cards" +
                " WHERE number = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, cardNumber);
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Проверяет существование в бд кода валюты
     *
     * @param currencyCode Код валюты
     * @return true - код валюты существует<br> false - код валюты не существует
     * @throws SQLException
     */
    public boolean currencyExists(String currencyCode) throws SQLException {
        String query = "SELECT count(*) as count from dictionaries.currencies" +
                " WHERE code = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, currencyCode);
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Проверяет существование в бд продукта
     *
     * @param productName Название продукта
     * @return true - продукт существует<br> false - продукт не существует
     * @throws SQLException
     */
    public boolean productExists(String productName) throws SQLException {
        String query = "SELECT count(*) as count from dictionaries.products" +
                " WHERE name = ?";
        statement = connection.prepareStatement(query);
        statement.setString(1, productName);
        getResultSet(query);
        return resultSet.getInt("count") > 0;
    }

    /**
     * Возвращает статус карты на момент создания заказа
     *
     * @param cardNumber Номер карты
     * @param orderDate  Дата заказа
     * @return Статус карты
     * @throws SQLException
     * @throws ParseException
     */
    public String getCardStatusForOrderDate(String cardNumber, String orderDate) throws SQLException, ParseException {
        String query = "SELECT ch.begin_date date, cs.status status from dictionaries.cards c" +
                " JOIN dictionaries.card_status_history ch on ch.card_id = c.id" +
                " JOIN dictionaries.card_statuses cs on cs.id = ch.status_id" +
                " WHERE c.number = ? ORDER BY date;";
        statement = connection.prepareStatement(query);
        statement.setString(1, cardNumber);
        getResultSet(query);
        Hashtable<String, String> cardStatuses = new Hashtable<>();
        List<String> dates = new ArrayList<>();
        do {
            cardStatuses.put(resultSet.getString("date"), resultSet.getString("status"));
            dates.add(resultSet.getString("date"));
        } while (resultSet.next());
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Long orderDateLong = format.parse(orderDate).getTime();
        if (format.parse(dates.get(0)).getTime() > orderDateLong) {
            return "undefinite";
        }
        if (orderDateLong >= format.parse(dates.get(dates.size() - 1)).getTime()) {
            return cardStatuses.get(dates.get(dates.size() - 1));
        }
        for (int i = 0; i < dates.size() - 1; i++) {
            if (format.parse(dates.get(i)).getTime() <= orderDateLong
                    && orderDateLong < format.parse(dates.get(i + 1)).getTime()) {
                return cardStatuses.get(dates.get(i));
            }
        }
        return "undefinite";
    }


    /**
     * Возвращает данные, необходимые для создания SPTA отчетов
     *
     * @return данные, агрегированные по имени торговой точки
     * @throws SQLException
     */
    public ResultSet getSalePointTotalAmountInfo() throws SQLException {
        String query = "SELECT sp.name, COUNT(*) count, SUM(ord.sum) sum" +
                " FROM processing.orders ord" +
                " JOIN dictionaries.sale_points sp ON sp.id = ord.sale_point_id" +
                " WHERE ord.rejected = 'N'" +
                " GROUP BY ord.sale_point_id" +
                " HAVING sum > 0";
        statement = connection.prepareStatement(query);
        getResultSet(query);
        return resultSet;
    }

    /**
     * Возвращает данные, необходимые для создания SPR отчетов
     *
     * @return данные, агрегированные по имени торговой точки и коду реджекта
     * @throws SQLException
     */
    public ResultSet getSalePointRejectsInfo() throws SQLException {
        String query = " SELECT sp.name, rej.code, rej.type, COUNT(*) count" +
                " FROM processing.orders ord" +
                " JOIN dictionaries.sale_points sp ON sp.id = ord.sale_point_id" +
                " JOIN processing.rejects rej ON rej.order_number = ord.number" +
                " GROUP BY sp.name, rej.code, rej.type";
        statement = connection.prepareStatement(query);
        getResultSet(query);
        return resultSet;
    }


    /**
     *
     * Возвращает данные, необходимые для расчета и начисления бонусов по выполненным заказам
     *
     * @return данные, агрегированные по номеру карты и номеру заказа
     * @throws SQLException
     */
    public ResultSet getBonusCardUseInfo() throws SQLException {
        String query = "SELECT c.number card_number, c.type card_type, op.order_number, sum(op.count * op.settl_price) sum" +
                " FROM processing.order_positions op" +
                " JOIN processing.orders o ON op.order_number = o.number" +
                " LEFT JOIN processing.loyalty_txns lt ON o.number = lt.order_number" +
                " JOIN dictionaries.cards c ON o.card_id = c.id" +
                " JOIN dictionaries.products p ON op.product_id = p.id" +
                " JOIN dictionaries.product_lines pl ON p.product_line_id = pl.id" +
                " WHERE op.rejected = 'N'" +
                " AND pl.name != 'ALCOHOL'" +
                " AND o.card_id IS NOT NULL" +
                " AND lt.id IS NULL" +
                " HAVING sum > 0 ";
        statement = connection.prepareStatement(query);
        getResultSet(query);
        return resultSet;
    }

    /**
     * Создает транзакцию с бонусами в рамках выполненного заказа
     * @param card номер карты
     * @param cardType тип карты
     * @param orderNumber номер заказа
     * @param settlSum сумма заказа
     * @param bonusSum сумма начисляемых бонусов
     * @throws SQLException
     */
    public void createLoyaltyTxn(String card, String cardType, int orderNumber, double settlSum, int bonusSum) throws SQLException {
        String query = "INSERT INTO processing.loyalty_txns(card, card_type, order_number, settl_sum, bonus_sum)" +
                " VALUES (?,?,?,?,?)";
        statement = connection.prepareStatement(query);
        statement.setString(1, card);
        statement.setString(2, cardType);
        statement.setInt(3, orderNumber);
        statement.setDouble(4, settlSum);
        statement.setInt(5, bonusSum);
        statement.execute(query);
    }


    public void createLogRecord(String command, String result, String error) throws SQLException {
        String query = "INSERT INTO processing.command_log(command, result, error)" +
                        " VALUES (?,?,?)";
//        query = error.equals("") ? query + "null)" : query + "'" + error + "')";
        statement = connection.prepareStatement(query);
        statement.setString(1, command);
        statement.setString(2, result);
        String error1 = error.equals("") ? null : error;
        statement.setString(3, error1);
        statement.execute(query);

    }

    public void cleanProcessingData() throws SQLException {
        String query = "DELETE FROM processing.orders;";
        statement = connection.prepareStatement(query);
        statement.execute(query);
        statement.execute("DELETE FROM processing.files;");
        statement.execute("DELETE FROM processing.command_log;");
    }

    public void sendDataToArchive() throws SQLException {
        String query = "INSERT INTO archive.orders (number, sale_point_id, order_date, card_id, ccy_id, sum)" +
                " SELECT number, sale_point_id, order_date, card_id, ccy_id, sum  FROM processing.orders;";
        statement = connection.prepareStatement(query);
        statement.execute(query);
        query = " INSERT INTO archive.order_positions (order_number, product_id, settl_price, count)" +
                " SELECT order_number, product_id, settl_price, count FROM processing.order_positions;";
        statement.execute(query);
        query = " INSERT INTO archive.order_indicators (order_number, indicator)" +
                " SELECT order_number, indicator FROM processing.order_indicators;";
        statement.execute(query);
        query = " INSERT INTO archive.loyalty_txns (card, order_number, bonus_sum)" +
                " SELECT card, order_number, bonus_sum FROM processing.loyalty_txns;";
        statement.execute(query);
    }

    public ResultSet getOrderIndicators(int orderNumber) throws SQLException {
        String query = "SELECT indicator FROM processing.order_indicators WHERE order_number = ?";
        statement = connection.prepareStatement(query);
        statement.setInt(1, orderNumber);
        getResultSet(query);
        return resultSet;
    }
}

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
    private Statement statement = null;
    private ResultSet resultSet;

    /**
     * Создание подключения к бд
     */
    public MySQLDb() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
                            SystemProperties.get("db.server"),
                            SystemProperties.get("db.scheme"),
                            SystemProperties.get("db.user"),
                            SystemProperties.get("db.password")));
            statement = connection.createStatement();
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
        String query = "SELECT id FROM test.sale_points WHERE name = '" + salePointName + "'";
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
        String query = "SELECT id FROM test.cards WHERE number = '" + cardNumber + "'";
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
     * @throws SQLException
     */
    public int getProductId(String productName) throws SQLException {
        String query = "SELECT id FROM test.products WHERE name = '" + productName + "'";
        getResultSet(query);
        return resultSet.getInt("id");
    }

    /**
     * Возвращает название продуктовой линии продукта по названию продукта
     *
     * @param productName Название продукта
     * @return Название продуктовой линии
     * @throws SQLException
     */
    public String getProductLine(String productName) throws SQLException {
        String query = "SELECT name FROM test.product_lines WHERE id = " +
                "(SELECT product_line_id FROM test.products WHERE name = '" + productName + "')";
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
        String query = "SELECT file_id FROM test.files WHERE name = '" + fileName + "'";
        getResultSet(query);
        return resultSet.getInt("file_id");
    }


    /**
     * Проверяет, существует ли файл в бд по его имени
     *
     * @param fileName Имя файла
     * @return true - файл существует<br> false - файл не существует
     * @throws SQLException
     */
    public boolean fileExists(String fileName) throws SQLException {
        String query = "SELECT count(*) as x from test.files WHERE " +
                "name = '" + fileName + "';";
        getResultSet(query);
        return resultSet.getInt("x") != 0;
    }

    /**
     * Создает запись о файле в бд
     *
     * @param fileName Имя файла
     * @param status   Статус файла
     * @throws SQLException
     */
    public void createFile(String fileName, OrderFileStatus status) throws SQLException {
        int fileId = NumGenerator.generate(8);
        String query = "INSERT INTO test.files (file_id, name, status)" +
                " VALUES (" +
                "'" + fileId + "'" +
                ", " +
                "'" + fileName + "'" +
                ", " +
                "'" + status + "'" +
                ");";
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
        String query = "UPDATE test.files" +
                " SET " +
                "status = '" + status + "'" +
                " WHERE " +
                "name = '" + fileName + "'" +
                ";";
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
        StringBuilder sb;
        int orderNumber = order.getOrderNum();
        double orderSum = 0.0;
        if (String.valueOf(rejected).equals("N")) {
            for (OrderPosition position : order.getPositions()) {
                if (OrderFileValidator.validateOrderPosition(fileName, order.getOrderNum(), order.getSalePoint(), position)) {
                    createOrderPosition(position, orderNumber, order.getCurrency(), order.getDate(), 'N');
                } else {
                    createOrderPosition(position, orderNumber, order.getCurrency(), order.getDate(), 'Y');
                }
            }
            sb = new StringBuilder("SELECT SUM(settl_price) AS order_sum FROM test.order_positions WHERE order_number ='")
                    .append(orderNumber).append("';");
            getResultSet(sb.toString());
            orderSum = resultSet.getDouble("order_sum");
        }
        String query = "INSERT INTO test.orders (number, sale_point_id, order_date, card_id, " +
                "file_id, ccy_id, sum, rejected, sale_point_order_num)" +
                " VALUES (" +
                orderNumber +
                ", " +
                "'" + getSalePointId(order.getSalePoint()) + "'" +
                ", " +
                "DATE_FORMAT('" + order.getDate() + "', '%Y-%m-%d %H:%i:%s')" +
                ", " +
                getCardId(order.getCard()) +
                ", " +
                "'" + getFileId(fileName) + "'" +
                ", " +
                "'" + getCurrencyId(order.getCurrency()) + "'" +
                ", " +
                "'" + orderSum + "'" +
                ", " +
                "'" + rejected + "'" +
                ", " +
                "'" + order.getSalePointOrderNum() + "'" +
                ");";
        statement.execute(query);
        if (!order.getIndicators().isEmpty()) {
            createOrderIndicators(order.getIndicators(), orderNumber);
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
        String query = "SELECT count(*) count FROM test.orders" +
                " WHERE order_date = '" +
                order.getDate() +
                "'" +
                " AND sale_point_id = '" +
                getSalePointId(order.getSalePoint()) +
                "'" +
                " AND sale_point_order_num = '" +
                order.getSalePointOrderNum() +
                "'" +
                "';";
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
        String query = "SELECT id FROM test.currencies WHERE code = '" + currencyСode + "'";
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
        String query = "INSERT INTO test.order_positions (order_number, product_id, orig_price, settl_price, count, rejected)" +
                " VALUES (" +
                orderId +
                ", " +
                getProductId(orderPosition.getProduct()) +
                ", " +
                orderPosition.getPrice() +
                ", " +
                settlPrice +
                ", " +
                orderPosition.getCount() +
                ", " +
                "'" + rejected + "'" +
                ");";
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
        String query = "SELECT course FROM test.currency_courses" +
                " WHERE ccy_id = '" +
                getCurrencyId(currencyCode) +
                "'" +
                " AND date = '" +
                //TODO сделать нормальную конвертацию даты
                courseDate.substring(0, 10) +
                "';";
        getResultSet(query);
        return resultSet.getDouble("course");
    }

    /**
     * Создает записи в бд обо всех индикаторах заказа
     *
     * @param orderIndicators Индикаторы заказа
     * @param orderId         Идентификатор заказа в бд
     * @throws SQLException
     */
    private void createOrderIndicators(Set<String> orderIndicators, int orderId) throws SQLException {
        orderIndicators.forEach(indicator ->
        {
            StringBuilder query = new StringBuilder("INSERT INTO test.order_indicators (order_id, indicator)")
                    .append(" VALUES (")
                    .append(orderId)
                    .append(", ").append("'").append(indicator).append("'")
                    .append(");");
            try {
                statement.execute(query.toString());
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
        String query = "INSERT INTO test.rejects (file_name, order_number, order_position_number, " +
                "type, code)" +
                " VALUES (" +
                "'" + fileName + "'" +
                ", null, null, " +
                "'" + RejectType.FILE + "'" +
                ", " +
                rejectCode +
                ");";
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
        String query = "INSERT INTO test.rejects (file_name, order_number, order_position_number, " +
                "type, code, incorrect_field_value)" +
                " VALUES (" +
                "'" + fileName + "'" +
                ", " + orderNumber +
                ", null, " +
                "'" + RejectType.ORDER + "'" +
                ", " +
                rejectCode +
                ", " +
                "'" + fieldValue + "'" +
                ");";
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
        String query = "INSERT INTO test.rejects (file_name, order_number, order_position_number, " +
                "type, code, incorrect_field_value)" +
                " VALUES (" +
                "'" + fileName + "'" +
                ", " + orderNumber +
                ", " + orderPositionNumber + ", " +
                "'" + RejectType.ORDER_POSITION + "'" +
                ", " +
                rejectCode +
                ", " +
                "'" + fieldValue + "'" +
                ");";
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
        String query = "SELECT count(*) as count from test.sale_point_agreements" +
                " WHERE type = 'LateDispatch'" +
                " AND sale_point_id =" +
                " (SELECT id FROM test.sale_points WHERE name = " +
                "'" + order.getSalePoint() + "');";
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
        String query = "SELECT count(*) as count from test.sale_point_agreements" +
                " WHERE type = 'ForeignCurrency'" +
                " AND sale_point_id =" +
                " (SELECT id FROM test.sale_points WHERE name = " +
                "'" + order.getSalePoint() + "');";
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
        String query = "SELECT count(*) as count from test.sale_point_agreements" +
                " WHERE type = 'NewProduct'" +
                " AND sale_point_id =" +
                " (SELECT id FROM test.sale_points WHERE name = " +
                "'" + salePoint + "');";
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
        String query = "SELECT count(*) as count from test.sale_points" +
                " WHERE name = " +
                "'" + salePoint + "';";
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
        String query = "SELECT count(*) as count from test.cards" +
                " WHERE number = " +
                "'" + cardNumber + "';";
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
        String query = "SELECT count(*) as count from test.currencies" +
                " WHERE code = " +
                "'" + currencyCode + "';";
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
        String query = "SELECT count(*) as count from test.products" +
                " WHERE name = " +
                "'" + productName + "';";
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
        String query = "SELECT ch.begin_date date, cs.status status from test.cards c" +
                " JOIN card_status_history ch on ch.card_id = c.id" +
                " JOIN card_statuses cs on cs.id = ch.status_id" +
                " WHERE c.number = " +
                "'" + cardNumber + "'" +
                " ORDER BY date;";
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


    public ResultSet getSalePointTotalAmountInfo() throws SQLException {
        String query = "SELECT sp.name, count(*) count, SUM(ord.sum) sum" +
                " FROM orders ord" +
                " JOIN sale_points sp on sp.id = ord.sale_point_id" +
                " GROUP BY ord.sale_point_id";
        getResultSet(query);
        return resultSet;
    }
}

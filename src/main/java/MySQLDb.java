import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Класс для работы с данными в бд Mysql
 */
public class MySQLDb {

    public Connection connection = null;
    private Statement statement = null;
    private Properties properties;

    /** Создание подключения к бд
     * @param properties Настройки подключения к бд
     */
    public MySQLDb(Properties properties) {
        try {
            this.properties = properties;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
                            properties.getProperty("db.server"),
                            properties.getProperty("db.scheme"),
                            properties.getProperty("db.user"),
                            properties.getProperty("db.password")));
        } catch (Exception e) {
            System.out.println("Ошибка при подключении к бд:\n" + e.getMessage());
        }
    }


    /**Возвращает идентификатор торговой точки по ее названию
     * @param salePointName Название торговой точки
     * @return Идентификатор торговой точки
     * @throws SQLException
     */
    public int getSalePointId(String salePointName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.sale_points WHERE name = '" + salePointName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        try {
            return resultSet.getInt("id");
        } catch (SQLException e){
            //TODO придумать нормальную реализацию
            return 0;
        }
    }

    /** Возвращает идентификатор карты по ее номеру
     * @param cardNumber Номер карты
     * @return Идентификатор карты
     * @throws SQLException
     */
    public int getCardId(String cardNumber) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.cards WHERE number = '" + cardNumber + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        try {
            return resultSet.getInt("id");
        } catch (SQLException e){
            //TODO придумать нормальные реализации (тег card отсутствует и карта не найдена)
            return 0;
        }
    }

    /** Возвращает идентификатор продукта по его названию
     * @param productName Название продукта
     * @return Идентификатор продукта
     * @throws SQLException
     */
    public int getProductId(String productName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.products WHERE name = '" + productName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    /** Возвращает название продуктовой линии продукта по названию продукта
     * @param productName Название продукта
     * @return Название продуктовой линии
     * @throws SQLException
     */
    public String getProductLine(String productName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT name FROM test.product_lines WHERE id = " +
                "(SELECT product_line_id FROM test.products WHERE name = '" + productName + "')";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getString("name");
    }

    /** Возвращает идентификатор файла по его имени
     * @param fileName Имя Файла
     * @return Идентификатор файла
     * @throws SQLException
     */
    public int getFileId(String fileName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT file_id FROM test.files WHERE name = '" + fileName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("file_id");
    }


    /** Проверяет, существует ли файл в бд по его имени
     * @param fileName Имя файла
     * @return true - файл существует<br> false - файл не существует
     * @throws SQLException
     */
    public boolean fileExists(String fileName) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as x from test.files WHERE ")
                .append("name = '" + fileName + "';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        if (resultSet.getInt("x") == 0) {
            return false;
        } else {
            return true;
        }
    }

    /** Создает запись о файле в бд
     * @param fileName Имя файла
     * @param status Статус файла
     * @throws SQLException
     */
    public void createFile(String fileName, OrderFileStatus status) throws SQLException {
        StringBuilder sb = new StringBuilder("");
        Random generator = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(generator.nextInt(10));
        }
        int fileId = Integer.parseInt(sb.toString());
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("INSERT INTO test.files (file_id, name, status)")
                .append(" VALUES (")
                .append("'" + fileId + "'")
                .append(", ")
                .append("'" + fileName + "'")
                .append(", ")
                .append("'" + status + "'")
                .append(");");
        statement.execute(query.toString());
    }

    /** Обновляет статус файла в бд
     * @param fileName Имя файла
     * @param status Новый статус файла
     * @throws SQLException
     */
    public void updateFileStatus(String fileName, OrderFileStatus status) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("UPDATE test.files")
                .append(" SET ")
                .append("status = '" + status + "'")
                .append(" WHERE ")
                .append("name = '" + fileName + "'")
                .append(";");
        statement.execute(query.toString());
    }

    /** Создает запись о заказе в бд
     * @param order Заказ
     * @param fileName Имя файла, в котором находится заказ
     * @param rejected true - Заказ отклонен, false - Заказ обработан без ошибок
     * @throws SQLException
     */
    public void createOrder(Order order, String fileName, char rejected) throws SQLException {
        StringBuilder sb;
        int orderNumber = order.getOrderNum();
        double orderSum = 0.0;
        statement = connection.createStatement();
        if (String.valueOf(rejected).equals("N")) {
            for (OrderPosition position : order.getPositions()) {
                if (OrderFileValidator.validateOrderPosition(fileName, order.getOrderNum(), order.getSalePoint(), position)) {
                    createOrderPosition(position, orderNumber, order.getCurrency(), order.getDate(), 'N');
                } else {
                    //создать orderPosition с реджектом
                }
            }
            sb = new StringBuilder("SELECT SUM(settl_price) AS order_sum FROM test.order_positions WHERE order_number ='")
                    .append(orderNumber).append("';");
            ResultSet resultSet = statement.executeQuery(sb.toString());
            resultSet.next();
            orderSum = resultSet.getDouble("order_sum");
        }
        StringBuilder query = new StringBuilder("INSERT INTO test.orders (number, sale_point_id, order_date, card_id, " +
                "file_id, ccy_id, sum, rejected, sale_point_order_num)")
                .append(" VALUES (")
                .append(orderNumber)
                .append(", ")
                .append("'" + getSalePointId(order.getSalePoint()) + "'")
                .append(", ")
                .append("DATE_FORMAT('" + order.getDate() + "', '%Y-%m-%d %H:%i:%s')")
                .append(", ")
                .append(getCardId(order.getCard()))
                .append(", ")
                .append("'" + getFileId(fileName) + "'")
                .append(", ")
                .append("'" + getCurrencyId(order.getCurrency()) + "'")
                .append(", ")
                .append("'" + orderSum + "'")
                .append(", ")
                .append("'" + rejected + "'")
                .append(", ")
                .append("'" + order.getSalePointOrderNum() + "'")
                .append(");");
        statement.execute(query.toString());
        if (!order.getIndicators().isEmpty()) {
            createOrderIndicators(order.getIndicators(), orderNumber);
        }
    }


    /** Проверяет, существует ли заказ в бд
     * @param order Заказ
     * @return true - Заказ существует в бд<br> false - Заказ не существует в бд
     * @throws SQLException
     */
    boolean orderExists(Order order) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) count FROM test.orders")
                .append(" WHERE order_date = '")
                .append(order.getDate())
                .append("'")
                .append(" AND sale_point_id = '")
                .append(getSalePointId(order.getSalePoint()))
                .append("'")
                .append(" AND sale_point_order_num = '")
                .append(order.getSalePointOrderNum())
                .append("'")
                .append("';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        if (resultSet.getInt("count") > 0) {
            return true;
        } else {
            return false;
        }
    }

    /** Возвращает идентификатор курса валюты по коду валюты
     * @param currencyСode Код валюты
     * @return Идентификатор валюты
     * @throws SQLException
     */
    private int getCurrencyId(String currencyСode) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.currencies WHERE code = '" + currencyСode + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        try {
            return resultSet.getInt("id");
        } catch (SQLException e){
            //TODO придумать нормальную реализацию
            return 0;
        }
    }

    /** Создает запись в бд о позиции в заказе
     * @param orderPosition Позиция в заказе
     * @param orderId Идентификатор заказа в бд
     * @param currencyCode Код валюты заказа
     * @param orderDate Дата заказа
     * @param rejected true - Позиция заказа отклонена, false - Позиция заказа обработана без ошибок
     * @throws SQLException
     */
    private void createOrderPosition(OrderPosition orderPosition, int orderId, String currencyCode, String orderDate, char rejected) throws SQLException {
        statement = connection.createStatement();
        Double settlPrice = getCurrencyCourse(currencyCode, orderDate) * Double.parseDouble(orderPosition.getPrice());
        StringBuilder query = new StringBuilder(
                "INSERT INTO test.order_positions (order_number, product_id, orig_price, settl_price, count, rejected)")
                .append(" VALUES (")
                .append(orderId)
                .append(", ")
                .append(getProductId(orderPosition.getProduct()))
                .append(", ")
                .append(orderPosition.getPrice())
                .append(", ")
                .append(settlPrice)
                .append(", ")
                .append(orderPosition.getCount())
                .append(", ")
                .append("'" + rejected + "'")
                .append(");");
        statement.execute(query.toString());
    }


    /** Возвращает курс валюты на конкретную дату
     * @param currencyCode Код валюты
     * @param courseDate Дата, на которую необходимо определить курс
     * @return Курс валюты
     * @throws SQLException
     */
    public Double getCurrencyCourse(String currencyCode, String courseDate) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT course FROM test.currency_courses")
                .append(" WHERE ccy_id = '")
                .append(getCurrencyId(currencyCode))
                .append("'")
                .append(" AND date = '")
                //TODO сделать нормальную конвертацию даты
                .append(courseDate.substring(0, 10))
                .append("';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getDouble("course");
    }

    /** Создает записи в бд обо всех индикаторах заказа
     * @param orderIndicators Индикаторы заказа
     * @param orderId Идентификатор заказа в бд
     * @throws SQLException
     */
    private void createOrderIndicators(Set<String> orderIndicators, int orderId) throws SQLException {
        statement = connection.createStatement();
        orderIndicators.forEach(indicator ->
        {
            StringBuilder query = new StringBuilder("INSERT INTO test.order_indicators (order_id, indicator)")
                    .append(" VALUES (")
                    .append(orderId)
                    .append(", ")
                    .append("'" + indicator + "'")
                    .append(");");
            try {
                statement.execute(query.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /** Создает запись об отклонении файла от обработки
     * @param fileName Имя файла
     * @param rejectCode Код ошибки из-за которой файл отклоняется от обработки
     * @throws SQLException
     */
    public void createRejectForFile(String fileName, int rejectCode) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("INSERT INTO test.rejects (file_name, order_number, order_position_number, ")
                .append("type, code)")
                .append(" VALUES (")
                .append("'" + fileName + "'")
                .append(", null, null, ")
                .append("'" + RejectType.FILE + "'")
                .append(", ")
                .append(rejectCode)
                .append(");");
        statement.execute(query.toString());
    }


    /** Создает запись об отклонении заказа от обработки
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNumber Системный номер заказа
     * @param rejectCode Код ошибки из-за которой заказ отклоняется от обработки
     * @param fieldValue Значение поля из-за которого произошла ошибка
     * @throws SQLException
     */
    public void createRejectForOrder(String fileName, int orderNumber, int rejectCode, String fieldValue) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("INSERT INTO test.rejects (file_name, order_number, order_position_number, ")
                .append("type, code, incorrect_field_value)")
                .append(" VALUES (")
                .append("'" + fileName + "'")
                .append(", " + orderNumber)
                .append(", null, ")
                .append("'" + RejectType.ORDER + "'")
                .append(", ")
                .append(rejectCode)
                .append(", ")
                .append("'" + fieldValue + "'")
                .append(");");
        statement.execute(query.toString());
    }

    /** Создает запись об отклонении позиции заказа от обработки
     * @param fileName Имя файла, в котором находится заказ
     * @param orderNumber Системный номер заказа, в котором находится позиция заказа
     * @param orderPositionNumber Порядковый номер позиции в заказе
     * @param rejectCode Код ошибки из-за которой позиция заказа отклоняется от обработки
     * @param fieldValue Значение поля из-за которого произошла ошибка
     * @throws SQLException
     */
    public void createRejectForOrderPosition(String fileName, int orderNumber, int orderPositionNumber, int rejectCode,
                                             String fieldValue) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("INSERT INTO test.rejects (file_name, order_number, order_position_number, ")
                .append("type, code, incorrect_field_value)")
                .append(" VALUES (")
                .append("'" + fileName + "'")
                .append(", " + orderNumber)
                .append(", " + orderPositionNumber + ", ")
                .append("'" + RejectType.ORDER_POSITION + "'")
                .append(", ")
                .append(rejectCode)
                .append(", ")
                .append("'" + fieldValue + "'")
                .append(");");
        statement.execute(query.toString());
    }

    /** Проверяет наличие соглашения у торговой точки на позднее представление заказов на обработку
     * @param order Заказ, в рамках которого происходит проверка
     * @return true - соглашение имеется<br> false - соглашение отсутствует
     * @throws SQLException
     */
    public boolean lateDispatchAgreement(Order order) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as count from test.sale_point_agreements")
                .append(" WHERE type = 'LateDispatch'")
                .append(" AND sale_point_id =")
                .append(" (SELECT id FROM test.sale_points WHERE name = ")
                .append("'" + order.getSalePoint() + "');");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getInt("count") > 0;
    }

    /** Проверяет наличие соглашения у торговой точки на представление заказов с иностранной валютой на обработку
     * @param order Заказ, в рамках которого происходит проверка
     * @return true - соглашение имеется<br> false - соглашение отсутствует
     * @throws SQLException
     */
    public boolean foreignCurrencyAgreement(Order order) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as count from test.sale_point_agreements")
                .append(" WHERE type = 'ForeignCurrency'")
                .append(" AND sale_point_id =")
                .append(" (SELECT id FROM test.sale_points WHERE name = ")
                .append("'" + order.getSalePoint() + "');");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getInt("count") > 0;
    }

    /** Проверяет наличие соглашения у торговой точки на представление заказов с неизвестным продуктом на обработку
     * @param salePoint Название торговой точки
     * @return true - соглашение имеется<br> false - соглашение отсутствует
     * @throws SQLException
     */
    public boolean newProductAgreement(String salePoint) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as count from test.sale_point_agreements")
                .append(" WHERE type = 'NewProduct'")
                .append(" AND sale_point_id =")
                .append(" (SELECT id FROM test.sale_points WHERE name = ")
                .append("'" + salePoint + "');");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getInt("count") > 0;
    }

    /** Проверяет существование в бд торговой точки
     * @param salePoint Название торговой точки
     * @return true - торговая точка существует<br> false - торговая точка не существует
     * @throws SQLException
     */
    public boolean salePointExists(String salePoint) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as count from test.sale_points")
                .append(" WHERE name = ")
                .append("'" + salePoint + "';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getInt("count") > 0;
    }

    /** Проверяет существование в бд карты
     * @param cardNumber Номер карты
     * @return true - карта существует<br> false - карта не существует
     * @throws SQLException
     */
    public boolean cardExists(String cardNumber) throws SQLException {
        //проверяет только, если тег card существует и заполнен
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as count from test.cards")
                .append(" WHERE number = ")
                .append("'" + cardNumber + "';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getInt("count") > 0;
    }

    /** Проверяет существование в бд кода валюты
     * @param currencyCode Код валюты
     * @return true - код валюты существует<br> false - код валюты не существует
     * @throws SQLException
     */
    public boolean currencyExists(String currencyCode) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as count from test.currencies")
                .append(" WHERE code = ")
                .append("'" + currencyCode + "';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getInt("count") > 0;
    }

    /** Проверяет существование в бд продукта
     * @param productName Название продукта
     * @return true - продукт существует<br> false - продукт не существует
     * @throws SQLException
     */
    public boolean productExists(String productName) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT count(*) as count from test.products")
                .append(" WHERE name = ")
                .append("'" + productName + "';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        resultSet.next();
        return resultSet.getInt("count") > 0;
    }

    /** Возвращает статус карты на момент создания заказа
     * @param cardNumber Номер карты
     * @param orderDate Дата заказа
     * @return Статус карты
     * @throws SQLException
     * @throws ParseException
     */
    public String getCardStatusForOrderDate(String cardNumber, String orderDate) throws SQLException, ParseException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT ch.begin_date date, cs.status status from test.cards c")
                .append(" JOIN card_status_history ch on ch.card_id = c.id")
                .append(" JOIN card_statuses cs on cs.id = ch.status_id")
                .append(" WHERE c.number = ")
                .append("'" + cardNumber + "'")
                .append(" ORDER BY date;");
        ResultSet resultSet = statement.executeQuery(query.toString());
        Hashtable<String, String> cardStatuses = new Hashtable<>();
        List<String> dates = new ArrayList<>();
        while (resultSet.next()){
            cardStatuses.put(resultSet.getString("date"), resultSet.getString("status"));
            dates.add(resultSet.getString("date"));
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Long orderDateLong = format.parse(orderDate).getTime();
        if(format.parse(dates.get(0)).getTime() > orderDateLong){
            return "undefinite";
        }
        if(orderDateLong >= format.parse(dates.get(dates.size() - 1)).getTime()){
            return cardStatuses.get(dates.get(dates.size() - 1));
        }
        for(int i = 0; i < dates.size() - 1; i++){
            if(format.parse(dates.get(i)).getTime() <= orderDateLong
                    && orderDateLong < format.parse(dates.get(i + 1)).getTime()){
                return cardStatuses.get(dates.get(i));
            }
        }
        return "undefinite";
    }
}

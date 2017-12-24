import java.sql.*;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class MySQLDb {

    public Connection connection = null;
    private Statement statement = null;
    private Properties properties;

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


    public int getSalePointId(String salePointName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.sale_points WHERE name = '" + salePointName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    public int getCardId(String cardName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.cards WHERE number = '" + cardName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    public int getProductId(String productName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.products WHERE name = '" + productName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    public String getProductLine(String productName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT name FROM test.product_lines WHERE id = " +
                "(SELECT product_line_id FROM test.products WHERE name = '" + productName + "')";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getString("name");
    }

    public int getFileId(String fileName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT file_id FROM test.files WHERE name = '" + fileName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("file_id");
    }


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

    public void createOrder(Order order, String fileName, char rejected) throws SQLException {
        StringBuilder sb;
        int orderNumber = order.getOrderNum();
        double orderSum = 0.0;
        statement = connection.createStatement();
        if (String.valueOf(rejected).equals("N")){
            for (OrderPosition position : order.getPositions()) {
                createOrderPosition(position, orderNumber, order.getCurrency(), order.getDate(), 'N');
            }
            sb = new StringBuilder("SELECT SUM(settl_price) AS order_sum FROM test.order_positions WHERE order_number ='")
                    .append(orderNumber).append("';");
            ResultSet resultSet = statement.executeQuery(sb.toString());
            resultSet.next();
            orderSum = resultSet.getDouble("order_sum");
        }
        StringBuilder query = new StringBuilder("INSERT INTO test.orders (number, sale_point_id, order_date, card_id, file_id, ccy_id, sum, rejected)")
                .append(" VALUES (")
                .append(orderNumber)
                .append(", ")
                .append("'" + getSalePointId(order.getSalePoint()) + "'")
                .append(", ")
                .append("DATE_FORMAT('" + order.getDate() + "', '%Y-%m-%d %H:%i:%s')")
                .append(", ")
                .append(order.getCard() == null ? "null" : getCardId(order.getCard()))
                .append(", ")
                .append("'" + getFileId(fileName) + "'")
                .append(", ")
                .append("'" + getCurrencyId(order.getCurrency()) + "'")
                .append(", ")
                .append("'" + orderSum + "'")
                .append(", ")
                .append("'" + rejected + "'")
                .append(");");
        statement.execute(query.toString());
        if (!order.getIndicators().isEmpty()) {
            createOrderIndicators(order.getIndicators(), orderNumber);
        }
    }

    private int getCurrencyId(String currency) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.currencies WHERE code = '" + currency + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

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


}

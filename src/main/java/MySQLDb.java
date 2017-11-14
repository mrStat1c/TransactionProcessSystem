import java.sql.*;
import java.util.Properties;
import java.util.Random;

public class MySQLDb {

    public Connection connection = null;
    private Statement statement = null;
    private Properties properties;

    public MySQLDb(Properties properties){
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


    public int getSalePointId (String salePointName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.sale_points WHERE name = '" + salePointName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    public int getCardId (String cardName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.cards WHERE number = '" + cardName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    public int getProductId (String productName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.products WHERE name = '" + productName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    public int getFileId (String fileName) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT file_id FROM test.files WHERE name = '" + fileName + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("file_id");
    }


    public boolean fileExists (String fileName) throws SQLException {
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

    public void createFile (String fileName, OrderFile.status status) throws SQLException {
        StringBuilder sb = new StringBuilder("");
        Random generator = new Random();
        for(int i = 0; i < 8; i++){
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

    public void updateFileStatus (String fileName, OrderFile.status status) throws SQLException {
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("UPDATE test.files")
                .append(" SET ")
                .append("status = '" + status + "'")
                .append(" WHERE ")
                .append("name = '" + fileName + "'")
                .append(";");
        statement.execute(query.toString());
    }

    public void createOrder (Order order, String fileName) throws SQLException {
        StringBuilder sb = new StringBuilder("");
        Random generator = new Random();
        for(int i = 0; i < 8; i++){
            sb.append(generator.nextInt(10));
        }
        int orderId = Integer.parseInt(sb.toString());
        statement = connection.createStatement();
        StringBuilder query = new StringBuilder("INSERT INTO test.orders (order_id, sale_point_id, order_date, card_id, file_id, ccy_id)")
                .append(" VALUES (")
                .append(orderId)
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
                .append(");");
        statement.execute(query.toString());
        for (OrderPosition position: order.getPositions()){
            createOrderPosition(position, orderId);
        }
    }

    private int getCurrencyId(String currency) throws SQLException {
        statement = connection.createStatement();
        String query = "SELECT id FROM test.currencies WHERE ccy_code = '" + currency + "'";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt("id");
    }

    private void createOrderPosition(OrderPosition orderPosition, int orderId) throws SQLException {
            statement = connection.createStatement();
            StringBuilder query = new StringBuilder("INSERT INTO test.order_positions (order_id, product_id, price, count)")
                    .append(" VALUES (")
                    .append(orderId)
                    .append(", ")
                    .append(getProductId(orderPosition.getProduct()))
                    .append(", ")
                    .append(orderPosition.getPrice())
                    .append(", ")
                    .append(orderPosition.getCount())
                    .append(");");
            statement.execute(query.toString());
        }
}

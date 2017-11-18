import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;


public class IndicatorStamper {

    private boolean alcoholInd;
    private boolean lotteryInd;
    private double lotteryMinSum;
    private MySQLDb db;

    public IndicatorStamper(Properties properties, MySQLDb db) throws IOException {
        this.db = db;
        this.alcoholInd = Boolean.parseBoolean(properties.getProperty("validator.alcohol"));
        this.lotteryInd = Boolean.parseBoolean(properties.getProperty("validator.lottery"));
        this.lotteryMinSum = Double.parseDouble(properties.getProperty("validator.lotteryMinSum"));
    }

    public Order processOrder(Order order) throws SQLException {
        if (alcoholInd) {
            for (OrderPosition position : order.getPositions()) {
                if (db.getProductLine(position.getProduct()).equals("ALCOHOL")) {
                    order.addIndicator(OrderIndicator.ALCOHOL);
                    break;
                }
            }
        }
        if (lotteryInd){
            double orderSum = 0;
            for (OrderPosition position : order.getPositions()) {
                orderSum += Double.parseDouble(position.getPrice()) *
                        Double.parseDouble(position.getCount());
            }
            if (orderSum >= lotteryMinSum){
                order.addIndicator(OrderIndicator.LOTTERY);
            }
        }
        return order;
    }
}

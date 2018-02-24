import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class IndicatorStamper {

    private boolean alcoholInd;
    private boolean lotteryInd;
    private double lotteryMinSum;
    private boolean productLineInd;
    private double productLineMinSum;
    private MySQLDb db;
    private static final Logger log = LogManager.getLogger(IndicatorStamper.class.getName());

    public IndicatorStamper(Properties properties, MySQLDb db) throws IOException {
        this.db = db;
        this.alcoholInd = Boolean.parseBoolean(properties.getProperty("validator.alcohol"));
        this.lotteryInd = Boolean.parseBoolean(properties.getProperty("validator.lottery"));
        this.lotteryMinSum = Double.parseDouble(properties.getProperty("validator.lotteryMinSum"));
        this.productLineInd = Boolean.parseBoolean(properties.getProperty("validator.productLine"));
        this.productLineMinSum = Double.parseDouble(properties.getProperty("validator.productLineMinSum"));
    }

    public Order processOrder(Order order) throws SQLException {
        Double currencyCourse = db.getCurrencyCourse(order.getCurrency(), order.getDate());
        if (alcoholInd) {
            for (OrderPosition position : order.getPositions()) {
                if (db.getProductLine(position.getProduct()).equals("ALCOHOL")) {
                    order.addIndicator(OrderIndicator.ALCOHOL);
                    log.info("Order" + order.getSalePointOrderNum() + "is stamped with indicator " + OrderIndicator.ALCOHOL);
                    break;
                }
            }
        }
        if (lotteryInd){
            double orderSum = 0;
            for (OrderPosition position : order.getPositions()) {
                orderSum += Double.parseDouble(position.getPrice()) * currencyCourse
                        * Double.parseDouble(position.getCount());
            }
            if (orderSum >= lotteryMinSum){
                order.addIndicator(OrderIndicator.LOTTERY);
                log.info("Order" + order.getSalePointOrderNum() + "is stamped with indicator " + OrderIndicator.LOTTERY);
            }
        }
        if (productLineInd){
            double positionSum;
            String prodLine;
            Map<String, Double> prodLineGroups = new HashMap<>();
            for (OrderPosition position: order.getPositions()){
                prodLine = db.getProductLine(position.getProduct());
                positionSum = Double.parseDouble(position.getPrice()) * currencyCourse
                * Double.parseDouble(position.getCount());
                prodLineGroups.put(prodLine, prodLineGroups.getOrDefault(prodLine, 0.0) + positionSum);
            }
            prodLineGroups.entrySet().stream()
                    .filter(entry -> entry.getValue() >= productLineMinSum)
                    .forEach(entry -> {
                        order.addIndicator(OrderIndicator.PROD_LINE, ".".concat(entry.getKey()));
                        log.info("Order" + order.getSalePointOrderNum() + "is stamped with indicator " +
                                OrderIndicator.PROD_LINE.toString().concat(".").concat(entry.getKey()));
                    });
        }
        return order;
    }
}

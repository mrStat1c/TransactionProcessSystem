package processing;

import model.Order;
import model.OrderIndicator;
import model.OrderPosition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Добавляет к model.Order индикаторы OrderIndicators
 */
public class IndicatorStamper {

    private boolean alcoholInd;
    private boolean lotteryInd;
    private double lotteryMinSum;
    private boolean productLineInd;
    private double productLineMinSum;
    private MySQLDb db;
    private static final Logger log = LogManager.getLogger(IndicatorStamper.class.getName());

    /**
     * @param db Объект для работы с данными в бд
     */
    IndicatorStamper(MySQLDb db) {
        this.db = db;
        this.alcoholInd = Boolean.parseBoolean(SystemProperties.get("validator.alcohol"));
        this.lotteryInd = Boolean.parseBoolean(SystemProperties.get("validator.lottery"));
        this.lotteryMinSum = Double.parseDouble(SystemProperties.get("validator.lotteryMinSum"));
        this.productLineInd = Boolean.parseBoolean(SystemProperties.get("validator.productLine"));
        this.productLineMinSum = Double.parseDouble(SystemProperties.get("validator.productLineMinSum"));
    }

    /**
     * В зависимости от условий, добавляет к model.Order индикаторы OrderIndicators
     */
    public Order processOrder(Order order) throws SQLException {
        Double currencyCourse = db.getCurrencyCourse(order.getCurrency(), order.getDate());
        if (alcoholInd) {
            for (OrderPosition position : order.getPositions()) {
                if (db.getProductLine(position.getProduct()).equals("ALCOHOL")) {
                    order.addIndicator(OrderIndicator.ALCOHOL);
                    log.info("model.Order " + order.getSalePointOrderNum() + " is stamped with indicator " + OrderIndicator.ALCOHOL);
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
                log.info("model.Order" + order.getSalePointOrderNum() + "is stamped with indicator " + OrderIndicator.LOTTERY);
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
                        log.info("model.Order" + order.getSalePointOrderNum() + "is stamped with indicator " +
                                OrderIndicator.PROD_LINE.toString().concat(".").concat(entry.getKey()));
                    });
        }
        return order;
    }
}

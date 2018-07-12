import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoyaltyModule {

    private static Logger log = LogManager.getLogger(LoyaltyModule.class.getName());
    private static MySQLDb db = new MySQLDb();


    class OrderData{
        private String cardNumber;
        private String cardType;
        private int orderNumber;
        private double sum;

        OrderData(String cardNumber, String cardType, int orderNumber, double sum){
            this.cardNumber = cardNumber;
            this.cardType = cardType;
            this.orderNumber = orderNumber;
            this.sum = sum;
        }
    }

    public void createBonusTxns() throws SQLException {
        List<OrderData> orderDataList = new ArrayList<>();
        int bonusSum = 0;
        ResultSet resultSet = db.getBonusCardUseInfo();
        do {
            orderDataList.add(new OrderData(
                    resultSet.getString("card_number"),
                    resultSet.getString("card_type"),
                    resultSet.getInt("order_number"),
                    resultSet.getDouble("sum")));
        } while (resultSet.next());
        for(OrderData orderData: orderDataList){
            switch (orderData.cardType) {
                case "Green": bonusSum = bonusSumCalculate(orderData.sum, 3); break;
                case "Silver": bonusSum = bonusSumCalculate(orderData.sum, 5); break;
                case "Gold": bonusSum = bonusSumCalculate(orderData.sum, 7); break;
                default: log.error("Unknown cardType.");
            }
            if (bonusSum > 0) {
                db.createLoyaltyTxn(orderData.cardNumber,
                        orderData.cardType,
                        orderData.orderNumber,
                        orderData.sum,
                        bonusSum);
            }
        }
    }

    private static int bonusSumCalculate(double sum, int percent) {
        return (int) (sum * percent / 100);
    }
}

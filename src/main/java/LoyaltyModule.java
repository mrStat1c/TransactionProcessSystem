import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoyaltyModule {

    private static Logger log = LogManager.getLogger(LoyaltyModule.class.getName());
    private static MySQLDb db = new MySQLDb();

    public static void createBonusTxns() throws SQLException {
        String cardNumber, cardType;
        int orderNumber, bonusSum = 0;
        double rubSum;
        ResultSet resultSet = db.getBonusCardUseInfo();
        while (resultSet.next()) {
             cardNumber = resultSet.getString("card_number");
             cardType = resultSet.getString("card_type");
             orderNumber = resultSet.getInt("order_number");
             rubSum = resultSet.getDouble("sum");
            switch (cardType) {
                case "Green": bonusSum = bonusSumCalculate(rubSum, 3); break;
                case "Silver": bonusSum = bonusSumCalculate(rubSum, 5); break;
                case "Gold": bonusSum = bonusSumCalculate(rubSum, 7); break;
                default: log.error("Unknown cardType.");
            }
            if (bonusSum > 0) {
                db.createLoyaltyTxn(cardNumber, cardType, orderNumber, rubSum, bonusSum);
            }
        }
    }

    private static int bonusSumCalculate(double sum, int percent) {
        return (int) (sum * percent / 100);
    }
}

package processing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.NumGenerator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Класс для расчета и начисления бонусов за заказы
 */
public class LoyaltyModule {

    private static Logger log = LogManager.getLogger(LoyaltyModule.class.getName());
    private static MySQLDb db = new MySQLDb();


    /**
     * Инкапсулирует данные по заказу, необходимые для начисления бонусов
     */
    class OrderData {
        private String cardNumber;
        private String cardType;
        private int orderNumber;
        private double sum;

        OrderData(String cardNumber, String cardType, int orderNumber, double sum) {
            this.cardNumber = cardNumber;
            this.cardType = cardType;
            this.orderNumber = orderNumber;
            this.sum = sum;
        }
    }

    /**
     * Создает транзакции с бонусами
     */
    public void createBonusTxns() throws SQLException {
        List<OrderData> orderDataList = new ArrayList<>();
        int bonusPercent;
        ResultSet resultSet = db.getBonusCardUseInfo();
        if(resultSet.getRow()==0){
            log.info("No data for Bonus Txns creating.");
            return;
        }
        do {
            orderDataList.add(new OrderData(
                    resultSet.getString("card_number"),
                    resultSet.getString("card_type"),
                    resultSet.getInt("order_number"),
                    resultSet.getDouble("sum")));
        } while (resultSet.next());
        for (OrderData orderData : orderDataList) {
            switch (orderData.cardType) {
                case "Green": bonusPercent = 3; break;
                case "Silver": bonusPercent = 5; break;
                case "Gold": bonusPercent = 7; break;
                default: {
                    log.error("Unknown cardType.");
                    continue;
                }
            }
            if(winningOrder(orderData.orderNumber)){
                bonusPercent *= 2;
            }
            db.createLoyaltyTxn(
                    orderData.cardNumber,
                    orderData.cardType,
                    orderData.orderNumber,
                    orderData.sum,
                    bonusSumCalculate(orderData.sum, bonusPercent)
            );
        }
        log.info("Bonus Txns were created.");
    }

    /**
     * Рассчитывает количество начисляемых бонусов
     *
     * @param sum     - сумма заказа
     * @param percent - количество процентов бонусов от суммы заказа
     * @return количество бонусов
     */
    private static int bonusSumCalculate(double sum, int percent) {
        return (int) (sum * percent / 100);
    }

    private static boolean winningOrder(int orderNumber) throws SQLException {
        ResultSet resultSet = db.getOrderIndicators(orderNumber);
        if (resultSet.getRow() > 0) {
            do {
                if (resultSet.getString("indicator").equals("LOTTERY")
                        && NumGenerator.generate(1) != 9) {
                    return true;
                }
            } while (resultSet.next());
        }
        return false;
    }
}

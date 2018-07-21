import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Подготавливает и хранит данные, необходимые для создания отчетов
 */
public class ReportDataHolder {

    private static Logger log = LogManager.getLogger(ReportDataHolder.class.getName());
    private static MySQLDb db = new MySQLDb();
    private static ResultSet resultSet;
    private static List<SalePointTotalAmountInfo> salePointsTotalAmountInfo = new ArrayList<>();
    private static List<SalePointRejectsInfo> salePointsRejectsInfo = new ArrayList<>();

    public static List<SalePointTotalAmountInfo> getSalePointsTotalAmountInfo() {
        return salePointsTotalAmountInfo;
    }

    public static List<SalePointRejectsInfo> getSalePointsRejectsInfo() {
        return salePointsRejectsInfo;
    }

    /**
     * Подготавливает данные, необходимые для SPTA отчета
     */
    public static void prepareSPTAReportData() {
        try {
            resultSet = db.getSalePointTotalAmountInfo();
            do {
                salePointsTotalAmountInfo.add(new SalePointTotalAmountInfo(
                        resultSet.getString("name"),
                        resultSet.getInt("count"),
                        resultSet.getDouble("sum")
                ));
            } while (resultSet.next());
        } catch (SQLException e) {
            log.error("SPTA report could not create.\n " + e.getMessage());
        }
    }

    /**
     * Подготавливает данные, необходимые для SPR отчета
     */
    public static void prepareSPRReportData() {
        try {
            resultSet = db.getSalePointRejectsInfo();
            Map<String, Map<String, Integer>> orderRejectsInfo = new HashMap<>();
            Map<String, Map<String, Integer>> orderPositionRejectsInfo = new HashMap<>();
            String salePointName, rejectCode;
            int rejectCount;
            Map<String, Integer> tempMap;

            do {
                salePointName = resultSet.getString("name");
                rejectCode = resultSet.getString("code");
                rejectCount = resultSet.getInt("count");

                if (resultSet.getString("type").equals(RejectType.ORDER.toString())) {
                    tempMap = orderRejectsInfo.containsKey(salePointName) ?
                            orderRejectsInfo.get(salePointName) : new HashMap<>();
                    tempMap.put(rejectCode, rejectCount);
                    orderRejectsInfo.put(salePointName, tempMap);
                } else {
                    tempMap = orderPositionRejectsInfo.containsKey(salePointName) ?
                            orderPositionRejectsInfo.get(salePointName) : new HashMap<>();
                    tempMap.put(rejectCode, rejectCount);
                    orderPositionRejectsInfo.put(salePointName, tempMap);
                }
            } while (resultSet.next());

            Set<String> keySet = new HashSet<>();
            keySet.addAll(orderRejectsInfo.keySet());
            keySet.addAll(orderPositionRejectsInfo.keySet());
            keySet.forEach(salePoint ->
                    salePointsRejectsInfo.add(new SalePointRejectsInfo(
                            salePoint,
                            orderRejectsInfo.get(salePoint),
                            orderPositionRejectsInfo.get(salePoint)))
            );
        } catch (SQLException e) {
            log.error("SPR report could not create.\n " + e.getMessage());
        }
    }
}

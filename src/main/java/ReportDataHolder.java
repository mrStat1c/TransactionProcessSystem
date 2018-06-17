import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class ReportDataHolder {

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

    public static void prepareSPTAReportData() throws SQLException {
        resultSet = db.getSalePointTotalAmountInfo();
        do {
            salePointsTotalAmountInfo.add(new SalePointTotalAmountInfo(
                    resultSet.getString("name"),
                    resultSet.getInt("count"),
                    resultSet.getDouble("sum")
            ));
        } while (resultSet.next());
    }


    public static void prepareSPRReportData() throws SQLException {
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
    }

//    public static void main(String[] args) throws SQLException {
//        prepareSPRReportData();
//    }
}

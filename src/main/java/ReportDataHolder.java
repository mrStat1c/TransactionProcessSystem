import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ReportDataHolder {

    private static MySQLDb db = new MySQLDb();
    private static ResultSet resultSet;
    private static List<SalePointTotalAmountInfo> salePointsTotalAmountInfo = new ArrayList<>();
    private static List<SalePointRejectsInfo> salePointsRejectsInfo = new ArrayList<>();

    public static List<SalePointTotalAmountInfo> getSalePointsTotalAmountInfo (){
        return salePointsTotalAmountInfo;
    }

    public static List<SalePointRejectsInfo> getSalePointsRejectsInfo() {
        return salePointsRejectsInfo;
    }

    public static void prepareSPTAReportData() throws SQLException {
         resultSet = db.getSalePointTotalAmountInfo();
        do{
            salePointsTotalAmountInfo.add(new SalePointTotalAmountInfo(
                    resultSet.getString("name"),
                    resultSet.getInt("count"),
                    resultSet.getDouble("sum")
            ));
        } while (resultSet.next());
    }


    public static void prepareSPRReportData() throws SQLException {
      resultSet = db.getSalePointRejectsInfo();
//      Делаем Map<String name, Map<String code, int count>>
        Map<String, Map<String, Integer>> tempMap = new HashMap<>();
        String salePointName, rejectCode;
        int rejectCount;
        do{
            salePointName = resultSet.getString("name");
            rejectCode = resultSet.getString("code");
            rejectCount = resultSet.getInt("count");
          if (tempMap.containsKey(salePointName)){
              Map<String, Integer> rejectsMap = tempMap.get(salePointName);
              rejectsMap.put(rejectCode, rejectCount);
              tempMap.put(salePointName, rejectsMap);
          } else {
              tempMap.put(salePointName, Map.of(rejectCode, rejectCount));
          }
      } while (resultSet.next());

//      Превращаем Map<String, Map> в List<RejectInfo>
//        TODO не хватает второй Map с OrderPositions
        for(String salePoint: tempMap.keySet()){
            salePointsRejectsInfo.add(new SalePointRejectsInfo(
                    salePoint,
                    tempMap.get(salePoint),
                    new HashMap<>()));
        }
    }
}

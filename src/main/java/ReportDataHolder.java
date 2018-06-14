import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDataHolder {

    private static List<SalePointTotalAmountInfo> salePointsTotalAmountInfo = new ArrayList<>();

    public static List<SalePointTotalAmountInfo> getSalePointsTotalAmountInfo (){
        return salePointsTotalAmountInfo;
    }

    public static void prepareSPTAReportData() throws SQLException {
        MySQLDb db = new MySQLDb();
        ResultSet resultSet = db.getSalePointTotalAmountInfo();
        do{
            salePointsTotalAmountInfo.add(new SalePointTotalAmountInfo(
                    resultSet.getString("name"),
                    resultSet.getInt("count"),
                    resultSet.getDouble("sum")
            ));
        } while (resultSet.next());
    }
}

import java.util.ArrayList;
import java.util.List;

public class SalePointTotalAmountInfoHolder {

    private List<SalePointTotalAmountInfo> salePointsTotalAmountInfo = new ArrayList<>();

    public void addSalePointTotalAmountInfo(String salePointName, int orderCount, double orderTotalSum) {
        salePointsTotalAmountInfo.add(new SalePointTotalAmountInfo(salePointName, orderCount, orderTotalSum));
    }

}

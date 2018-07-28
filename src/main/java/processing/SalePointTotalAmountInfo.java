package processing;

/**
 * Инкапсулирует данные, необходимые для формирования SPTA отчета
 */
public class SalePointTotalAmountInfo {

    private String salePointName;
    private int orderCount;
    private double orderTotalSum;

    public SalePointTotalAmountInfo(String salePointName, int orderCount, double orderTotalSum) {
        this.salePointName = salePointName;
        this.orderCount = orderCount;
        this.orderTotalSum = orderTotalSum;
    }

    public String getSalePointName() {
        return salePointName;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public double getOrderTotalSum() {
        return orderTotalSum;
    }
}

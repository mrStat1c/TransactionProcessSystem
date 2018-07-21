import java.util.Map;

/**
 * Инкапсулирует данные, необходимые для формирования SPR отчета
 */
public class SalePointRejectsInfo {

    private String salePointName;
    private Map<String, Integer> orderRejects;
    private Map<String, Integer> orderPositionRejects;

    public SalePointRejectsInfo(String salePointName,
                                Map<String, Integer> orderRejects,
                                Map<String, Integer> orderPositionRejects){
     this.salePointName = salePointName;
     this.orderRejects = orderRejects;
     this.orderPositionRejects = orderPositionRejects;
    }

    public String getSalePointName() {
        return salePointName;
    }

    public void setSalePointName(String salePointName) {
        this.salePointName = salePointName;
    }

    public Map<String, Integer> getOrderRejects() {
        return orderRejects;
    }

    public Map<String, Integer> getOrderPositionRejects() {
        return orderPositionRejects;
    }
}

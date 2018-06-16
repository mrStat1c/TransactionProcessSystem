import java.util.HashMap;
import java.util.Map;

public class SalePointRejectsInfo {

    private String salePointName;
    private Map<String, Integer> orderRejects = new HashMap<>();
    private Map<String, Integer> orderPositionRejects = new HashMap<>();

    public SalePointRejectsInfo(String salePointName,
                                Map<String, Integer> orderRejects,
                                Map<String, Integer> orderPositionRejectsRejects){
     this.salePointName = salePointName;
     this.orderRejects = orderRejects;
     this.orderPositionRejects = orderPositionRejectsRejects;
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

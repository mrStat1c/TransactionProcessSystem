import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Order {

    private String sale_point;
    private String card;
    private String date;
    private List<OrderPosition> positions;
    private String currency;
    private Set<OrderIndicator> indicators = new HashSet<>();

    public Order(String sale_point, String card, String date, List<OrderPosition> positions, String currency){
        this.sale_point = sale_point;
        this.card = card;
        this.date = date;
        this.positions = positions;
        this.currency = currency;
    }

    public String getSalePoint(){
        return this.sale_point;
    }

    public String getCard(){
        return this.card;
    }

    public String getDate(){
        return this.date;
    }

    public List<OrderPosition> getPositions(){
        return this.positions;
    }

    public String getCurrency(){return this.currency;}

    public Set<OrderIndicator> getIndicators(){return this.indicators;}

    public void addIndicator(OrderIndicator indicator){
        this.indicators.add(indicator);
    }
}

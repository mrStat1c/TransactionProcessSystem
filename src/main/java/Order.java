import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Order {

    private String sale_point;
    private String card;
    private String date;
    private List<OrderPosition> positions;
    private String currency;
    private Set<String> indicators = new HashSet<>();
    private int orderNum;

    public Order(String sale_point, String card, String date, List<OrderPosition> positions, String currency){
        this.sale_point = sale_point;
        this.card = card;
        this.date = date;
        this.positions = positions;
        this.currency = currency;
        StringBuilder sb = new StringBuilder("");
        Random generator = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(generator.nextInt(10));
        }
        this.orderNum = Integer.parseInt(sb.toString());
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

    public Set<String> getIndicators(){return this.indicators;}

    public void addIndicator(OrderIndicator indicator){
        this.indicators.add(indicator.toString());
    }

    public void addIndicator(OrderIndicator indicator, String subField){
        this.indicators.add(indicator + subField);
    }

    public int getOrderNum() {
        return this.orderNum;
    }
}

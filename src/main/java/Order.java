import java.util.List;

public class Order {

    private String sale_point;
    private String card;
    private String date;
    private List<OrderPosition> positions;

    public Order(String sale_point, String card, String date, List<OrderPosition> positions){
        this.sale_point = sale_point;
        this.card = card;
        this.date = date;
        this.positions = positions;
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
}

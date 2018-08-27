package orderModel;

import utils.NumGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс, инкапсулирующий заказ
 */
public class Order {

    private String salePoint;
    private String card;
    private String date;
    private List<OrderPosition> positions;
    private String currency;
    private String salePointOrderNum;
    private Set<String> indicators = new HashSet<>();
    private int orderNum;

    public Order(String sale_point, String card, String date, List<OrderPosition> positions, String currency, String salePointOrderNum){
        this.salePoint = sale_point;
        this.card = card;
        this.date = date;
        this.positions = positions;
        this.currency = currency;
        this.salePointOrderNum = salePointOrderNum;
        this.orderNum = NumGenerator.generate(8);
    }

    public String getSalePoint(){
        return this.salePoint;
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

    /**
     * Добавляет к заказу индикатор
     *
     * @param indicator Индикатор
     */
    public void addIndicator(OrderIndicator indicator){
        this.indicators.add(indicator.toString());
    }

    /**
     *  Добавляет к заказу индикатор, имеющий подполе
     *
     * @param indicator Индикатор
     * @param subField Подполе
     */
    public void addIndicator(OrderIndicator indicator, String subField){
        this.indicators.add(indicator + subField);
    }

    public int getOrderNum() {
        return this.orderNum;
    }

    public String getSalePointOrderNum() {
        return salePointOrderNum;
    }
}

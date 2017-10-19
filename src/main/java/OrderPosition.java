/**
 * Created by Static on 30.07.2017.
 */
public class OrderPosition {

    private String product;
    private String price;
    private String count;

    public OrderPosition(String product, String price, String count){
        this.product = product;
        this.price = price;
        this.count = count;
    }

    public String getProduct(){
        return this.product;
    }

    public String getPrice(){
        return this.price;
    }

    public String getCount(){
        return this.count;
    }
}


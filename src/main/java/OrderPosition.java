/**
 * Класс, инкапсулирующий позицию в заказе
 */
public class OrderPosition {

    private String product;
    private String price;
    private String count;
    private boolean newProductInd;
    private int number;

    public OrderPosition(String product, String price, String count, boolean newProductInd, int number){
        this.product = product;
        this.price = price;
        this.count = count;
        this.newProductInd = newProductInd;
        this.number = number;
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

    public boolean getNewProductInd() {
        return this.newProductInd;
    }

    public int getNumber() {
        return this.number;
    }
}


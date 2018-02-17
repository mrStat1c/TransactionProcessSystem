public class OrderPosition {

    private String product;
    private String price;
    private String count;
    private boolean newProductInd;

    public OrderPosition(String product, String price, String count, boolean newProductInd){
        this.product = product;
        this.price = price;
        this.count = count;
        this.newProductInd = newProductInd;
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
}


import java.io.IOException;
import java.util.Properties;


public class OrderValidator {

    private boolean alcoholIndicator;


    public OrderValidator(Properties properties) throws IOException {
        this.alcoholIndicator = Boolean.parseBoolean(properties.getProperty("validator.alcohol"));
    }

    public void check(Order order){
        //проверки
    }
}

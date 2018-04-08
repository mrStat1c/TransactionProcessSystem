import java.util.Random;

public class NumGenerator {

    public static int generate (int numberCount){
        StringBuilder sb = new StringBuilder("");
        Random generator = new Random();
        for (int i = 0; i < numberCount; i++) {
            sb.append(generator.nextInt(10));
        }
        return Integer.parseInt(sb.toString());
    }
}

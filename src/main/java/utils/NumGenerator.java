package utils;

import java.util.Random;

/**
 * Класс для генерации случайных чисел
 */
public class NumGenerator {

    /**
     * Генерирует случайное целое число заданного количества цифр
     *
     * @param numberCount количество цифр
     * @return случайное целое число
     */
    public static int generate (int numberCount){
        StringBuilder sb = new StringBuilder("");
        Random generator = new Random();
        for (int i = 0; i < numberCount; i++) {
            sb.append(generator.nextInt(10));
        }
        return Integer.parseInt(sb.toString());
    }
}

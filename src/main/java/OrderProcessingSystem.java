import java.io.*;
import java.util.Properties;

/**
 * Класс, инкапсулирующий системные настройки
 */
class OrderProcessingSystem {

    public Properties systemProperties;
    private File systemPropertiesFile = new File("C:\\ClrSystemConfig\\config.ini");

    OrderProcessingSystem() throws IOException {
        systemProperties = new Properties();
        systemProperties.load(new FileInputStream(systemPropertiesFile));
    }
}

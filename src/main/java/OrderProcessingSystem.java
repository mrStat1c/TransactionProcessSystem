import java.io.*;
import java.util.Properties;

class OrderProcessingSystem {

    public Properties systemProperties;
    private File systemPropertiesFile = new File("C:\\ClrSystemConfig\\config.ini");

    OrderProcessingSystem() throws IOException {
        systemProperties = new Properties();
        systemProperties.load(new FileInputStream(systemPropertiesFile));
    }
}

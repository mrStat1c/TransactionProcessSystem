import java.io.*;
import java.util.Properties;

class ClearingSystem {

    public Properties systemProperties;
    private File systemPropertiesFile = new File("C:\\ClrSystemConfig\\config.ini");

    ClearingSystem() throws IOException {
        systemProperties = new Properties();
        systemProperties.load(new FileInputStream(systemPropertiesFile));
    }
}

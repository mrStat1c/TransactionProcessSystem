package processing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Класс, инкапсулирующий системные настройки
 */
class SystemProperties {

    private static Properties systemProperties = new Properties();
    private static final Logger log = LogManager.getLogger(SystemProperties.class.getName());

    static {
        try {
            String systemPropertiesPath = "C:\\ClrSystemConfig\\config.ini";
            systemProperties.load(new FileInputStream(new File(systemPropertiesPath)));
        } catch (IOException e) {
            log.error("File with system properties not found!");
            e.printStackTrace();
        }
    }

    public static String get(String propertyName){
        return systemProperties.getProperty(propertyName);
    }
}

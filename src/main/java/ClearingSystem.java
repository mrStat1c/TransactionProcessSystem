

import java.nio.file.*;
import java.util.Hashtable;

class ClearingSystem {
    Hashtable<String, String> systemProperties;

    public Path inputPath = Paths.get("C:\\ClrSystemInput");
    public Path completedPath = Paths.get("C:\\ClrSystemCompleted");

    ClearingSystem() {
        //собираем проперти из файла
    }


    String get(String property) {
        return systemProperties.get(property);
    }

}

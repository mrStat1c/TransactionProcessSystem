import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class SystemManager {

    private static Logger log = LogManager.getLogger(SystemManager.class.getName());
    private static MySQLDb db = new MySQLDb();

    private static Path inputPath = Paths.get(systemProperties.get("inputPath"));
    Path completedPath = Paths.get(systemProperties.get("completedPath"));
    Path failedPath = Paths.get(systemProperties.get("failedPath"));
    Path dublicatePath = Paths.get(systemProperties.get("dublicatePath"));
    private static Path rejectedPath = Paths.get(systemProperties.get("rejectedPath"));


    public static List<File> findFiles(Path path) {
        log.info("File processing is starting.");
        File [] files = path.toFile().listFiles();
        return files == null ? Collections.emptyList(): new ArrayList<>(Arrays.asList(files));
    }

    public static List<File> removeInvalidFiles(List<File> files) throws SQLException, IOException {
            for (int i = 0; i < files.size(); i++) {
                if (!OrderFileValidator.validateFile(files.get(i))) {
                    db.createFile(files.get(i).getName(), OrderFileStatus.REJECTED);
                    Files.move(inputPath.resolve(files.get(i).getName()),
                            rejectedPath.resolve(files.get(i).getName()));
                    log.info("File " + files.get(i).getName() + " rejected.");
                    files.remove(i);
                    i--;
                }
            }
       return files;
    }
}

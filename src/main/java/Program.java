import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;


public class Program {

    public static void main(String[] args) throws IOException, SQLException, ParseException {
        Logger log = LogManager.getLogger(Program.class.getName());

        List<File> files = SystemManager.removeInvalidFiles(SystemManager.findFiles());

        if (files.isEmpty()) {
            log.warn("No files for processing.");
            return;
        }

        SystemManager.startLoading(files);
        SystemManager.startUnloading();
        LoyaltyModule.createBonusTxns();
    }
}


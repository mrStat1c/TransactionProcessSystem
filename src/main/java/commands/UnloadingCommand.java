package commands;

import processing.SystemManager;
import java.io.IOException;
import java.sql.SQLException;

public class UnloadingCommand implements Command {

    @Override
    public boolean run() {
        String commandName = "Unloading";
        try {
            SystemManager.startUnloading();
        } catch (IOException e) {
            log.error("An error occurred while executing the command " + commandName + ".\n" + e.getMessage());
            try {
                db.createLogRecord(commandName, "ERROR", e.getMessage());
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        try {
            db.createLogRecord(commandName, "OK", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}

package commands;

import processing.SystemManager;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public class LoadingCommand implements Command {

    @Override
    public boolean run() {
        String commandName = "Loading";
        try {
            SystemManager.startLoading();
        } catch (SQLException | ParseException | IOException e) {
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

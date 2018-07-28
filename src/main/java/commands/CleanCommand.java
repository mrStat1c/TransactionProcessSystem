package commands;

import processing.SystemManager;

import java.sql.SQLException;

public class CleanCommand implements Command {
    @Override
    public boolean run() {
        String commandName = "Clean";
        try {
            SystemManager.clean();
        } catch (SQLException e) {
            log.error("An error occurred while executing the command " + commandName + ".\n" + e.getMessage());
            try {
                db.createLogRecord(commandName, "ERROR", e.getMessage());
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        return true;
    }
}

package commands;

import processing.SystemManager;

import java.sql.SQLException;

public class ArchivingCommand implements Command {

    @Override
    public boolean run() {
        String commandName = "Archiving";
        boolean result = true;
        String exceptionMsg = "";
        try {
            SystemManager.sendDataToArchive();
        } catch (SQLException e) {
            log.error("An error occurred while executing the command " + commandName + ".\n" + e.getMessage());
            result = false;
            exceptionMsg = e.getMessage();
        }
        try {
            db.createLogRecord(
                    commandName,
                    result ? "OK" : "ERROR",
                    result ? "" : exceptionMsg
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}

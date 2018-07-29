package commands;

import processing.LoyaltyModule;
import java.sql.SQLException;

public class BonusCalculateCommand implements Command {
    private LoyaltyModule loyaltyModule = new LoyaltyModule();

    @Override
    public boolean run() {
        String commandName = "BonusCalculate";
        boolean result = true;
        String exceptionMsg = "";
        try {
            loyaltyModule.createBonusTxns();
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

package commands;

import processing.LoyaltyModule;
import java.sql.SQLException;

public class BonusCalculateCommand implements Command {
    private LoyaltyModule loyaltyModule = new LoyaltyModule();

    @Override
    public boolean run() {
        String commandName = "BonusCalculate";
        try {
            loyaltyModule.createBonusTxns();
        } catch (SQLException e) {
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

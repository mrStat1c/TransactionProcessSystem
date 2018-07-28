package commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processing.MySQLDb;

public interface Command {

    Logger log = LogManager.getLogger(Command.class.getName());
    MySQLDb db = new MySQLDb();
    boolean run();
}

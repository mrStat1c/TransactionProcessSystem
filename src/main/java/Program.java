import commands.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class Program {

    public static void main(String[] args) {
        Logger log = LogManager.getLogger(Program.class.getName());

        Scanner in = new Scanner(System.in);
        log.info("Enter command...");
        String userInput = in.nextLine();
        Queue<Command> commands = new LinkedList<>();

        while (!userInput.isEmpty()){
        switch (userInput.split(" ")[0]){
            case "load_files": commands.add(new LoadingCommand()); break;
            case "unload_files":  commands.add(new UnloadingCommand()); break;
            case "bonus_calculate": commands.add(new BonusCalculateCommand()); break;
            case "clean": commands.add(new CleanCommand()); break;
            case "archiving": commands.add(new ArchivingCommand()); break;
            case "start": {
                commands.add(new LoadingCommand());
                commands.add(new UnloadingCommand());
                commands.add(new BonusCalculateCommand());
                break;
            }
            default: log.warn("Unknown command!");
        }
            while (!commands.isEmpty()) {
                if (!commands.poll().run()) {
                    break;
                }
            }

            log.info("Enter command...");
            userInput = in.nextLine();
        }
    }
}


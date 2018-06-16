import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;

public class ReportCreator {


    private static String outputPath = SystemProperties.get("outputPath");
    private static String currentDate = LocalDate.now().toString();
    private static Properties velocityProperties = new Properties();
    private static VelocityContext velocityContext;
    private static Template reportTemplate;
    private static String fullOutputPath;

    static {
        try {
            velocityProperties.load(new InputStreamReader(ReportCreator.class.getResourceAsStream("velocity.properties")));
            Velocity.init(velocityProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createReportSPTA() throws SQLException {

        ReportDataHolder.prepareSPTAReportData();

        velocityContext = new VelocityContext();
        velocityContext.put("spta", ReportDataHolder.getSalePointsTotalAmountInfo());
        reportTemplate = Velocity.getTemplate("ReportSPTA.vm", "UTF-8");

        //TODO придумать, как конфигурировать маску файла
        fullOutputPath = outputPath + "\\Report_SPTA_" + currentDate + ".txt";
        generatePhysFile();

    }

    public static void createReportSPR() throws SQLException {

        ReportDataHolder.prepareSPRReportData();
        velocityContext = new VelocityContext();
        velocityContext.put("spr", ReportDataHolder.getSalePointsRejectsInfo());
        reportTemplate = Velocity.getTemplate("ReportSPR.vm", "UTF-8");

        //TODO придумать, как конфигурировать маску файла
        fullOutputPath = outputPath + "\\Report_SPR_" + currentDate + ".txt";
        generatePhysFile();
    }

    private static void generatePhysFile(){
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fullOutputPath))) {
            reportTemplate.merge(velocityContext, bufferedWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

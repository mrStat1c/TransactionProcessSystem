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

    public static void createSPTAReport() throws SQLException, IOException {

        ReportDataHolder.prepareSPTAReportData();

        Properties velocityProperties = new Properties();
        velocityProperties.load(new InputStreamReader(ReportCreator.class.getResourceAsStream("velocity.properties")));
        Velocity.init(velocityProperties);
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("spta", ReportDataHolder.getSalePointsTotalAmountInfo());
        Template reportTemplate = Velocity.getTemplate("ReportSPTA.vm", "UTF-8");

        //TODO придумать, как конфигурировать маску файла
        String fullOutputPath = outputPath + "\\Report_SPTA_"  + currentDate + ".txt";

        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fullOutputPath))){
            reportTemplate.merge(velocityContext, bufferedWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

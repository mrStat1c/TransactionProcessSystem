import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

public class ReportCreator {

    public void initSPTAReport () throws SQLException {

        ReportDataHolder.prepareSPTAReportData();
        Velocity.init();
        VelocityContext velocityContext = new VelocityContext();
//        velocityContext.put("spta", ReportDataHolder.getSalePointsTotalAmountInfo());
        velocityContext.put("spta", ReportDataHolder.getSalePointsTotalAmountInfo().get(0));
        Template reportTemplate = Velocity.getTemplate("ReportSPTA.vm", "UTF-8");

        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out))){
            reportTemplate.merge(velocityContext, bufferedWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

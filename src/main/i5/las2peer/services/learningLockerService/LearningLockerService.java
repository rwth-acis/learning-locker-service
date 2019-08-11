package i5.las2peer.services.learningLockerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

@Api
@SwaggerDefinition(
    info = @Info(
        title = "Moodle Data Proxy Service",
        version = "1.0",
        description = "A proxy for requesting data from moodle",
        contact = @Contact(
            name = "Philipp Roytburg",
            email = "philipp.roytburg@rwth-aachen.de")))

/**
 * 
 * This service is for sending xAPI statements that are monitored through MobSOS to the defined Learning Locker instance.
 * 
 */
@ManualDeployment
@ServicePath("/lrs")
public class LearningLockerService extends Service {
    
    private String lrsDomain; // Learning Locker Domain
    private String lrsAuth; // Learning Locker Authentication
    
    private static ArrayList<String> oldstatements = new ArrayList<String>();
    
    public LearningLockerService(){
        setFieldValues();
    }
    
    /**
     * A function that is called by the user to send processed moodle to a mobsos data processing instance. 
     *
     * @param statements is an ArrayList of xAPI statements that are sent to the defined Learning Locker instance.
     * 
     */
    public void sendXAPIstatement(ArrayList<String> statements) {
        for(String statement : statements) {
            if(!oldstatements.contains(statement)) {
                oldstatements.add(statement);

                try {
                    URL url = new URL(lrsDomain);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("X-Experience-API-Version","1.0.3");
                    conn.setRequestProperty("Authorization", lrsAuth);
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setUseCaches(false);
                    
                    OutputStream os = conn.getOutputStream();
                    os.write(statement.getBytes("UTF-8"));
                    os.flush();
                    
                    conn.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}

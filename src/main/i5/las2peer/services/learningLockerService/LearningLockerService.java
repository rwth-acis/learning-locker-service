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
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

// TODO Describe your own service
/**
 * las2peer-Template-Service
 * 
 * This is a template for a very basic las2peer service that uses the las2peer WebConnector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in the SwaggerDefinition annotation to suit
 * your project. If you do not intend to provide a Swagger documentation of your service API, the entire Api and
 * SwaggerDefinition annotation should be removed.
 * 
 */
// TODO Adjust the following configuration
@Api
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Template Service",
				version = "1.0.0",
				description = "A las2peer Template Service for demonstration purposes.",
				termsOfService = "http://your-terms-of-service-url.com",
				contact = @Contact(
						name = "John Doe",
						url = "provider.com",
						email = "john.doe@provider.com"),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com")))
@ManualDeployment
@ServicePath("/lrs")
// TODO Your own service class
public class LearningLockerService extends Service {
	
	private String lrsDomain;
	private String lrsAuth;
	
	private static ArrayList<String> oldstatements = new ArrayList<String>();
	
	public LearningLockerService(){
		setFieldValues();
	}
	
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
					
					Reader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					
					//maybe for frontend needed maybe not
					for (int c; (c = reader.read()) >= 0;)
						System.out.print((char)c);
						
					
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

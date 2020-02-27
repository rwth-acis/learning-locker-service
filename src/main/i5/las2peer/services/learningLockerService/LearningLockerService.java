package i5.las2peer.services.learningLockerService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import javax.print.DocFlavor;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
    private String adminLrsAuth; // Learning Locker Authentication
    private String clientKey;
    private String clientSecret;
    private String lrsAdminId;
    private ArrayList<String> clientList = new ArrayList<>();

    
    private ArrayList<String> oldstatements = new ArrayList<String>();
    
    public LearningLockerService(){
        setFieldValues();
    }
    
    /**
     * A function that is called by the user to send processed moodle to a mobsos data processing instance. 
     *
     * @param statements is an ArrayList of xAPI statements that are sent to the defined Learning Locker instance.
     * 
     */
    public void sendXAPIstatement(ArrayList<String> statements ) throws IOException, ParseException {
        String lrsAuth = "";
//        String usertoken = extractTokenFromStatements(statements);


        for(String statement : statements) {
            if(!oldstatements.contains(statement)) {
                oldstatements.add(statement);
                String token = statement.split("\\*")[1];
                String xAPIStatement  = statement.split("\\*")[0];

                Object clientId =  searchIfIncomingClientExists(token);
                if(!(clientId).equals("newClient")) {
                    clientKey = (String) ((JSONObject) clientId).get("basic_key");
                    clientSecret = (String) ((JSONObject) clientId).get("basic_secret");
                    lrsAuth = Base64.getEncoder().encodeToString((clientKey+ ":" + clientSecret).getBytes());
                } else {
                    String storeId = getStoreIdOfAdmin();
                    Object newlyCreatedClient = createNewClient(token, storeId);
                    clientKey = (String) ((JSONObject) newlyCreatedClient).get("basic_key");
                    clientSecret = (String) ((JSONObject) newlyCreatedClient).get("basic_secret");
                    lrsAuth = Base64.getEncoder().encodeToString((clientKey+ ":" + clientSecret).getBytes());
                }

                try {
                    URL url = new URL(lrsDomain);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("X-Experience-API-Version","1.0.3");
                    conn.setRequestProperty("Authorization", "Basic " + lrsAuth);
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setUseCaches(false);
                    
                    OutputStream os = conn.getOutputStream();
                    os.write(xAPIStatement.getBytes("UTF-8"));
                    os.flush();

                    Reader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    
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

    //POST call to create a new client if does not exist

    //GET call to get the admin client store number
    private String getStoreIdOfAdmin() {
        String storeId = "";
        URL url = null;
        try {
            String clientURL = "http://aca518ec.ngrok.io/api/v2/client/" + lrsAdminId;
            url = new URL(clientURL);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-Experience-API-Version","1.0.3");
            conn.setRequestProperty("Authorization", adminLrsAuth);
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setUseCaches(false);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            System.out.println(reader);

            String line = "";
            StringBuilder response = new StringBuilder();

            while((line = reader.readLine()) != null) {
                response.append(line);
            }
            Object obj= JSONValue.parse(response.toString());
            storeId = obj != null ? (String) ((JSONObject) obj).get("lrs_id") : "";

        }  catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return storeId;
    }

    private Object searchIfIncomingClientExists(String moodleToken) throws IOException, ParseException {
        String storeId = "";
        URL url = null;
        try {
            String clientURL = "http://aca518ec.ngrok.io/api/v2/client/";
            url = new URL(clientURL);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-Experience-API-Version","1.0.3");
            conn.setRequestProperty("Authorization", adminLrsAuth);
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setUseCaches(false);

            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while((line = rd.readLine()) != null) {
                response.append(line);
            }
            Object obj= JSONValue.parse(response.toString());

            for(int i = 0 ;i < ((JSONArray ) obj).size(); i ++) {
                JSONObject client = (JSONObject) ((JSONArray) obj).get(i);
                if(client.get("title").equals("Client" + moodleToken)) {
                    return client.get("api");
                }
            }
        }  catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "newClient";
    }

    private Object createNewClient(String moodleToken, String storeId)  {
        URL url = null;
        try {
            url = new URL("http://aca518ec.ngrok.io/api/v2/client");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("X-Experience-API-Version","1.0.3");
        conn.setRequestProperty("Authorization", adminLrsAuth);
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setUseCaches(false);

        String clientName = moodleToken;
        String title = moodleToken;
        List<String> scopes =  new ArrayList<>();
        scopes.add("statements/read/mine");
        scopes.add("statements/write");


        NewClient newClient = new NewClient(clientName, storeId, title, scopes);
        ObjectMapper mapper = new ObjectMapper();

        String jsonString = mapper.writeValueAsString(newClient);
        OutputStream os = conn.getOutputStream();
        os.write(jsonString.getBytes("UTF-8"));
        os.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String line = "";
        StringBuilder response = new StringBuilder();

        while((line = reader.readLine()) != null) {
            response.append(line);
        }
        Object obj= JSONValue.parse(response.toString());
        return ((JSONObject) obj).get("api");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "" ;
    }

    private String extractTokenFromStatements(ArrayList<String> statements) {
        String token = "";
        if(statements != null ) {
         String firstStatement = statements.get(0);
        }
        return token;
    }
   /* public static void main(String args[]) throws IOException, ParseException {
        LearningLockerService lls  = new LearningLockerService();
        ArrayList<String> statements  = new ArrayList<>();
        statements.add("{\n" +
                "        \"userToken\": \"cc1d01f1a71ebf088246ec7bb3332e2c\",\n" +
                "    \"actor\": {\n" +
                "        \"mbox\": \"mailto:sana.jamal@rwth-aachen.de\",\n" +
                "        \"name\": \"Sana Jamal\",\n" +
                "        \"objectType\": \"Agent\"\n" +
                "    },\n" +
                "    \"verb\": {\n" +
                "        \"id\": \"http://adlnet.gov/expapi/verbs/completed\",\n" +
                "        \"display\": {\n" +
                "            \"en-US\": \"completed\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"object\": {\n" +
                "        \"id\": \"http://adlnet.gov/expapi/activities/example\",\n" +
                "        \"definition\": {\n" +
                "            \"name\": {\n" +
                "                \"en-US\": \"Quiz 1.a\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"objectType\": \"Activity\"\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "        \"score\": {\n" +
                "            \"scaled\": 0.9,\n" +
                "            \"raw\": 90,\n" +
                "            \"min\": 0,\n" +
                "            \"max\": 100\n" +
                "        },\n" +
                "        \"success\": true,\n" +
                "        \"completion\": true,\n" +
                "        \"duration\": \"P159D\"\n" +
                "    }\n" +
                "}");
        lls.sendXAPIstatement(statements);
    }*/
}

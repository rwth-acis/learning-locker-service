package i5.las2peer.services.learningLockerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.logging.L2pLogger;
import java.util.logging.Level;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

@Api
@SwaggerDefinition(
		info = @Info(
				title = "Learning Locker Service",
				version = "1.1.0",
				description = "A proxy for requesting data from moodle",
				contact = @Contact(
						name = "Boris Jovanovic",
						email = "jovanovic.boris@rwth-aachen.de")))

/**
 *
 * This service is for sending xAPI statements that are monitored through MobSOS to the defined Learning Locker
 * instance.
 *
 */
@ManualDeployment
@ServicePath("/lrs")
public class LearningLockerService extends Service {

	private String lrsDomain; // Learning Locker Domain
	private String lrsAuthAdmin; // Learning Locker Authentication
	private String lrsClientId; //
	// private ArrayList<String> clientList = new ArrayList<>();
	private final static String statementsEndpoint = "/data/xAPI/statements";
	private final static String clientEndpoint = "/api/v2/client/";
	private final static L2pLogger logger = L2pLogger.getInstance(LearningLockerService.class.getName());

	public LearningLockerService() {
		setFieldValues();
		L2pLogger.setGlobalConsoleLevel(Level.WARNING);
	}

	/**
	 * Upon a new mobsos data processing event the function sends the event data to the lrs.
	 *
	 * @param statements is an ArrayList of xAPI statements that are sent to the defined Learning Locker instance.
	 * @throws IOException if any I/O Exception occurs.
	 */
	public void sendXAPIstatement(ArrayList<String> statements) throws IOException {
		String lrsAuth = "";
		for (String statement : statements) {
			String token = statement.split("\\*")[1];
			String xAPIStatement = statement.split("\\*")[0];

			logger.warning("New Event using token " + token + ": " + xAPIStatement);

			// Checks if the client exists
			Object clientId = searchIfIncomingClientExists(token);

			if (!(clientId).equals("newClient")) {
				String clientKey = (String) ((JSONObject) clientId).get("basic_key");
				String clientSecret = (String) ((JSONObject) clientId).get("basic_secret");
				lrsAuth = Base64.getEncoder().encodeToString((clientKey + ":" + clientSecret).getBytes());
			} else {
				String storeId = getStoreIdOfAdmin();
				Object newlyCreatedClient = createNewClient(token, storeId);
				String clientKey = (String) ((JSONObject) newlyCreatedClient).get("basic_key");
				String clientSecret = (String) ((JSONObject) newlyCreatedClient).get("basic_secret");
				lrsAuth = Base64.getEncoder().encodeToString((clientKey + ":" + clientSecret).getBytes());
			}

			try {
				logger.warning("Forwarding event using lrsAuth: " + lrsAuth);
				URL url = new URL(lrsDomain + statementsEndpoint);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
				conn.setRequestProperty("Authorization", "Basic " + lrsAuth);
				conn.setRequestProperty("Cache-Control", "no-cache");
				conn.setUseCaches(false);

				OutputStream os = conn.getOutputStream();
				os.write(xAPIStatement.getBytes("UTF-8"));
				os.flush();

				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String line = "";
				StringBuilder response = new StringBuilder();

				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				logger.info(response.toString());

				conn.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// POST call to create a new client if does not exist

	// GET call to get the admin client store number
	private String getStoreIdOfAdmin() {
		String storeId = "";
		URL url = null;
		try {
			String clientURL = lrsDomain + clientEndpoint + lrsClientId;
			url = new URL(clientURL);
			HttpURLConnection conn = null;
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
			conn.setRequestProperty("Authorization", lrsAuthAdmin);
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setUseCaches(false);

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line = "";
			StringBuilder response = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			logger.info(response.toString());
			Object obj = JSONValue.parse(response.toString());
			storeId = obj != null ? (String) ((JSONObject) obj).get("lrs_id") : "";

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return storeId;
	}

	private Object searchIfIncomingClientExists(String moodleToken) throws IOException {
		String storeId = "";
		URL url = null;
		try {
			String clientURL = lrsDomain + clientEndpoint;
			url = new URL(clientURL);
			HttpURLConnection conn = null;
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
			conn.setRequestProperty("Authorization", lrsAuthAdmin);
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setUseCaches(false);

			InputStream is = conn.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				response.append(line);
			}
			Object obj = JSONValue.parse(response.toString());

			for (int i = 0; i < ((JSONArray) obj).size(); i++) {
				JSONObject client = (JSONObject) ((JSONArray) obj).get(i);
				if (client.get("title").equals(moodleToken)) {
					return client.get("api");
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "newClient";
	}

	private Object createNewClient(String moodleToken, String storeId) {
		URL url = null;
		try {
			url = new URL(lrsDomain + clientEndpoint);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
			conn.setRequestProperty("Authorization", lrsAuthAdmin);
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setUseCaches(false);

			logger.warning("Creating new user with Moodle token: " + moodleToken);

			String clientName = moodleToken;
			String title = moodleToken;
			List<String> scopes = new ArrayList<>();
			scopes.add("statements/read/mine");
			scopes.add("statements/write");

			NewClient newClient = new NewClient(clientName, storeId, title, scopes);
			ObjectMapper mapper = new ObjectMapper();

			String jsonString = mapper.writeValueAsString(newClient);
			logger.warning("Cleint Object: " + jsonString);
			OutputStream os = conn.getOutputStream();
			os.write(jsonString.getBytes("UTF-8"));
			os.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line = "";
			StringBuilder response = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			Object obj = JSONValue.parse(response.toString());
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
		return "";
	}
	
	public String getStatementsFromLRS(String token)  {
		StringBuffer response = new StringBuffer();
		String clientKey;
		String clientSecret;
		Object clientId = null;
		try {
			clientId = searchIfIncomingClientExists(token);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(!(clientId).equals("noClientExists")) {
			clientKey = (String) ((JSONObject) clientId).get("basic_key");
			clientSecret = (String) ((JSONObject) clientId).get("basic_secret");
			String lrsAuth = Base64.getEncoder().encodeToString((clientKey + ":" + clientSecret).getBytes());

			try {
				URL url = new URL(lrsDomain + "/data/xAPI/statements");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("X-Experience-API-Version","1.0.3");
				conn.setRequestProperty("Authorization", "Basic " + lrsAuth);
				conn.setRequestProperty("Cache-Control", "no-cache");

				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				conn.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return response.toString();
		} else {
			return "";
		}
	}
}

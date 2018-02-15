package lambda_api_motus.api_lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

/**
 * This Class Outputs JSON Response
 * 
 * @author gabriellucci
 *
 */
public class ProxyWithStream implements RequestStreamHandler {
	JSONParser parser = new JSONParser();

	public class DayData {
		public String day;
		public ArrayList<Integer> scores;

		public DayData() {
			this.day = "";
			this.scores = new ArrayList<Integer>();
		}

	}

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		// logger
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler of ProxyWithStream");
		logger.log(context.getInvokedFunctionArn());
		// reads input
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		JSONObject responseJson = new JSONObject();
		String usernamePara = "";
		String responseCode = "200";

		try {

			JSONObject event = (JSONObject) parser.parse(reader);

			if (event.get("pathParameters") != null) {
				JSONObject pps = (JSONObject) event.get("pathParameters");
				if (pps.get("username") != null) {
					usernamePara = (String) pps.get("username");
				}
			}

			// parses input and parameters

			// AWS Creds
			BasicAWSCredentials b = new BasicAWSCredentials("AKIAJM7TGXYSRZUJONWQ",
					"vg+ZEoSGLil4fyo39Q9ZKRoFtnv7cfaaANsW/qdc");
			// Get table
			AmazonDynamoDB client = new AmazonDynamoDBClient(b);// new
			client.setRegion(Region.getRegion(Regions.US_WEST_2));
			DynamoDB dynamoDB = new DynamoDB(client);
			ScanRequest scanRequest = new ScanRequest().withTableName("recordings");

			// Gets data from table
			ScanResult result = client.scan(scanRequest);
			JSONArray ja = new JSONArray();

			// ArrayList<DayData> graph = new ArrayList<DayData>();
			Map<String, ArrayList<Integer>> graph = new HashMap<String, ArrayList<Integer>>();

			// hm.put("Key1", values);

			// create json object
			for (Map<String, AttributeValue> item : result.getItems()) {

				String scoreStr = "";
				JSONObject temp = new JSONObject();
				String filename = item.get("filename").toString();
				filename = filename.substring(4, filename.length() - 2);
				String usernameDB = filename.substring(0, filename.length() - 25);
				if (usernameDB.equals(usernamePara)) {

					scoreStr = item.get("score").toString().replaceAll("[^\\d.]", "");
					int scoreInt = Integer.parseInt(scoreStr);
					scoreInt = (scoreInt + 1) * 20;
					String date = filename.substring(filename.length() - 25, filename.length() - 17);
					logger.log("Adding day: "+date);

					ArrayList<Integer> dayScores = graph.get(date);
					if (dayScores == null) {
						dayScores = new ArrayList<Integer>();
					}
					dayScores.add(scoreInt);
					graph.put(date, dayScores);
					

				}
			}
			int min = -1;
			int max = -1;
			for (Entry<String, ArrayList<Integer>> entry : graph.entrySet()) {
			    String key = entry.getKey();
			    ArrayList<Integer> value = entry.getValue();
			    int sum = 0;
				for(int score : value){
				    sum += score;
				}
				double avg= sum/value.size();
				if(Integer.parseInt(key)<min || min ==-1){
					min = Integer.parseInt(key);
				}
				if(Integer.parseInt(key)>max){
					max = Integer.parseInt(key);
				}
    			JSONObject temp = new JSONObject();
    			temp.put(key, avg);
				ja.add(temp);
				
				
				
			} 

		

			JSONObject responseBody = new JSONObject();
			responseBody.put("max", max);
			responseBody.put("min", min);

			responseBody.put("data", ja);

			// responseBody.put("message", greeting);

			JSONObject headerJson = new JSONObject();
			headerJson.put("x-custom-response-header", "my custom response header value");

			responseJson.put("statusCode", responseCode);
			responseJson.put("headers", headerJson);
			responseJson.put("body", responseBody.toString());

			logger.log(responseJson.toJSONString());
			OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
			writer.write(responseJson.toJSONString());
			writer.close();

		} catch (ParseException pex) {
			responseJson.put("statusCode", "400");
			responseJson.put("exception", pex);
		}
	}

}
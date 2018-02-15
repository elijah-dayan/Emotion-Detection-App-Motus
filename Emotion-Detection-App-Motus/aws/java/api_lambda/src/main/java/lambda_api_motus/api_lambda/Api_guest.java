package lambda_api_motus.api_lambda;

import java.io.IOException;
import java.util.Random;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
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
public class Api_guest implements RequestStreamHandler {
	JSONParser parser = new JSONParser();

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		// logger
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler of api_guest");
		logger.log(context.getInvokedFunctionArn());
		// reads input
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		JSONObject responseJson = new JSONObject();
		String name = "World";
		String responseCode = "200";

		Random rand = new Random();
		int randMax = 1000000;

		// parses input and parameters
		try {
			JSONObject event = (JSONObject) parser.parse(reader);
			if (event.get("queryStringParameters") != null) {
				JSONObject qps = (JSONObject) event.get("queryStringParameters");
				if (qps.get("name") != null) {
					name = (String) qps.get("name");
				}
				if (qps.get("httpStatus") != null) {
					responseCode = qps.get("httpStatus)").toString();
				}
			}

			// AWS Creds
			BasicAWSCredentials b = new BasicAWSCredentials("AKIAJM7TGXYSRZUJONWQ",
					"vg+ZEoSGLil4fyo39Q9ZKRoFtnv7cfaaANsW/qdc");
			// Get table
			AmazonDynamoDB client = new AmazonDynamoDBClient(b);// new
			client.setRegion(Region.getRegion(Regions.US_WEST_2));
			DynamoDB dynamoDB = new DynamoDB(client);
			ScanRequest scanRequest = new ScanRequest().withTableName("guest");

			// Gets data from table
			ScanResult result = client.scan(scanRequest);
			JSONArray ja = new JSONArray();
			int guestNum = 0;
			boolean isUnique = false;
			while (!isUnique) {
				guestNum = rand.nextInt(randMax) + 1;
				isUnique = true;
				// create json object
				for (Map<String, AttributeValue> item : result.getItems()) {
					int tableNum = Integer.parseInt(item.get("guestNumber").toString().replaceAll("[^\\d.]", ""));
					if(guestNum==tableNum){
						isUnique=false;
					}

				}
			}
			String guestID = "guest"+guestNum;
			
			//inserts into table
			try {
				System.out.println("Adding a new item...");
				Table table = dynamoDB.getTable("guest");

				PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("guestID", guestID)
						.withInt("guestNumber", guestNum)

				);

				System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

			} catch (Exception e) {
				System.err.println("Unable to add item: " + guestID);
				System.err.println(e.getMessage());
			}

			
			
			
			
			JSONObject responseBody = new JSONObject();
			responseBody.put("Recordings", guestID);

			JSONObject headerJson = new JSONObject();
			// headerJson.put("x-custom-response-header", "my custom response
			// header value");

			responseJson.put("statusCode", responseCode);
			responseJson.put("headers", headerJson);
			responseJson.put("body", responseBody.toString());
			responseJson = responseBody;
		} catch (ParseException pex) {
			responseJson.put("statusCode", "400");
			responseJson.put("exception", pex);
		}
		// writes data to output
		logger.log(responseJson.toJSONString());
		OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
		writer.write(responseJson.toJSONString());
		writer.close();
	}

}
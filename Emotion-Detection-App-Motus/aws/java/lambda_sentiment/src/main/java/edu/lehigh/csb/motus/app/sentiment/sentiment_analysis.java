package edu.lehigh.csb.motus.app.sentiment;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
/**
 * Gets text and calculates sentiment
 * @author gabriellucci
 *
 */
public class sentiment_analysis implements RequestHandler<SNSEvent, String> {

	@Override
	public String handleRequest(SNSEvent input, Context context) {
		//gets transcript
		context.getLogger().log("TESTT!");

		context.getLogger().log("Input: " + input);
//		System.err.println("INPUT=  " + input.getFilename() + " || " + input.getTranscript());
		String outputStr = "";
		String outputScore= "";
		
		//AWS creds
		BasicAWSCredentials b = new BasicAWSCredentials("AKIAJM7TGXYSRZUJONWQ",
				"	");
		String key = "Test";
		String tanscript = "";
		//get S3 file
		String messageStr = input.getRecords().get(0).getSNS().getMessage();
		tanscript = messageStr;

		Sentiment a = new Sentiment();
		
		// Runs NLP Sentiment Engine
		ArrayList<SentimentScore> output = a.findSentiment(tanscript);

		//converts score to human readable result
		for (int i = 0; i < output.size(); i++) {
			String outStr= null;
	        switch(output.get(i).score){
	        case 0: outStr = "very negative";
					break;
	        case 1: outStr = "negative";
	        		break;
	        case 2: outStr = "neutral";
					break;
	        case 3: outStr = "positive";
					break;
	        case 4: outStr = "very positive";
					break;
			default: outStr= "ERROR: Invalid Sentiment type";
					break;
	        
	        }
	        //combines each result
	        outputStr += outStr+"-";
			outputScore += output.get(i).score + "-";
		}
		// TODO: implement your handler
		System.err.println("output sentiment	=  " + outputStr);
		
		//Gets AmazonDB instance
		AmazonDynamoDB client = new AmazonDynamoDBClient(b);// new
		// EnvironmentVariableCredentialsProvider().getCredentials());
		client.setRegion(Region.getRegion(Regions.US_WEST_2));
		DynamoDB dynamoDB = new DynamoDB(client);
		Table table = dynamoDB.getTable("recordings");

		//inserts into table
		try {
			System.out.println("Adding a new item...");
			PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("filename", key)
					.withString("state", "done").withString("transcript",  tanscript)
					.withString("score", outputScore)

			);

			System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

		} catch (Exception e) {
			System.err.println("Unable to add item: " + key);
			System.err.println(e.getMessage());
		}

		return outputStr;
	}

}

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

public class sentiment_analysis implements RequestHandler<TranscriptInput, String> {

	@Override
	public String handleRequest(TranscriptInput input, Context context) {
		context.getLogger().log("Input: " + input);
		System.err.println("INPUT=  " + input.getFilename() + " || " + input.getTranscript());
		String outputStr = "";
		String outputScore= "";
		BasicAWSCredentials b = new BasicAWSCredentials("AKIAJM7TGXYSRZUJONWQ",
				"vg+ZEoSGLil4fyo39Q9ZKRoFtnv7cfaaANsW/qdc");
		String key = "";
		String tanscript = "";
		key = input.getFilename();
		tanscript = input.getTranscript();

		Sentiment a = new Sentiment();

		ArrayList<SentimentScore> output = a.findSentiment(tanscript);

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
	        outputStr += outStr+"-";
			outputScore += output.get(i).score + "-";
		}
		// TODO: implement your handler
		System.err.println("output sentiment	=  " + outputStr);

		AmazonDynamoDB client = new AmazonDynamoDBClient(b);// new
		// EnvironmentVariableCredentialsProvider().getCredentials());
		client.setRegion(Region.getRegion(Regions.US_WEST_2));
		DynamoDB dynamoDB = new DynamoDB(client);
		Table table = dynamoDB.getTable("recordings");

		try {
			System.out.println("Adding a new item...");
			PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("filename", key)
					.withString("duration", "dtest").withString("status", "done").withString("transcript",  input.getTranscript())
					.withString("score", outputScore).withString("score_string", outputStr)

			);

			System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

		} catch (Exception e) {
			System.err.println("Unable to add item: " + key);
			System.err.println(e.getMessage());
		}

		return outputStr;
	}

}

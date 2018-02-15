package com.amazonaws.lambda.csb;

import org.json.simple.JSONObject;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;


public class snsToNLP implements RequestHandler<SNSEvent, Object> {
	BasicAWSCredentials b = new BasicAWSCredentials("AKIAJM7TGXYSRZUJONWQ",
			"vg+ZEoSGLil4fyo39Q9ZKRoFtnv7cfaaANsW/qdc");
	public class TranscriptInput {

		private String filename;
		private String transcript;

		public String getFilename() {
			return filename;
		}

		public void setFilename(String value) {
			filename = value;
		}

		public String getTranscript() {
			return transcript;
		}

		public void setTranscript(String value) {
			transcript = value;
		}
	}

	public interface Transcript {
		@LambdaFunction(functionName = "sentiment-analysis")
		String doSentiment(TranscriptInput input);
	}


    @Override
    public Object handleRequest(SNSEvent input, Context context) {
        context.getLogger().log("Input: " + input);

		String key = "Test";
		String transcript = "";
		//get S3 file
		String messageStr = input.getRecords().get(0).getSNS().getMessage();
        context.getLogger().log("messageStr: " + messageStr);

		JSONParser parser = new JSONParser();
		
		JSONObject payload = null;
		try {
			payload = (JSONObject) parser.parse(messageStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        context.getLogger().log("Input: " + input);

		transcript = (String) payload.get("transcript");
		key = (String) payload.get("fileName");
        context.getLogger().log("transcript: " + transcript);
        context.getLogger().log("key: " + key);

        //DB update
        
        
        AmazonDynamoDB client = new AmazonDynamoDBClient(b);// new
		// EnvironmentVariableCredentialsProvider().getCredentials());
		client.setRegion(Region.getRegion(Regions.US_WEST_2));
		DynamoDB dynamoDB = new DynamoDB(client);
		Table table = dynamoDB.getTable("recordings");

		//inserts into table
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
		String nowAsISO = df.format(new Date());
        context.getLogger().log("nowAsISO: " + nowAsISO);

        try{		
		
			System.out.println("Adding a new item...");
			PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("filename", key)
					.withString("status", "analyzing").withString("transcript",  transcript)
					.withString("timeUploaded", nowAsISO));

			System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

		} catch (Exception e) {
			System.err.println("Unable to add item: " + key);
			System.err.println(e.getMessage());
		}
        
        //Lambda Invoke
        String json = "{\"filename\":\"" + key + "\",\"transcript\":\"" + transcript + "\"}";
		AWSLambda lanbdaCli = AWSLambdaClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(b))
				// s.setRegion(Region.getRegion(Regions.US_WEST_2))
				.build();

		final Transcript t = LambdaInvokerFactory.builder().lambdaClient(lanbdaCli)
				// .withRegion(Regions.US_WEST_2)
				.build(Transcript.class);
		TranscriptInput input2 = new TranscriptInput();
		input2.setFilename(key);
		input2.setTranscript(transcript);

		String temp = t.doSentiment(input2);
        return null;
    }

}

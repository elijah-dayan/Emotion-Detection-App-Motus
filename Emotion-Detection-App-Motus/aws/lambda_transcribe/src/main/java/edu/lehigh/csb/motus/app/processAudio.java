package edu.lehigh.csb.motus.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;

public class processAudio implements RequestHandler<S3Event, String> {
	BasicAWSCredentials b = new BasicAWSCredentials("AKIAJM7TGXYSRZUJONWQ", "vg+ZEoSGLil4fyo39Q9ZKRoFtnv7cfaaANsW/qdc");

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
	public String handleRequest(S3Event input, Context context) {
		context.getLogger().log("Input: " + input);
		S3EventNotificationRecord record = input.getRecords().get(0);
		String key = record.getS3().getObject().getKey();
		String transcript = "error	";
		AmazonDynamoDB client = new AmazonDynamoDBClient(b);
		client.setRegion(Region.getRegion(Regions.US_WEST_2));
		DynamoDB dynamoDB = new DynamoDB(client);
		Table table = dynamoDB.getTable("recordings");
		
		PutItemOutcome outcome = table
				.putItem(new Item().withPrimaryKey("filename", key).withString("duration", "dtest")
						.withString("status", "Transcribing")

		);
		System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

		// AmazonS3 s3Client = new AmazonS3Client(b);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(b))
				.withRegion(Regions.US_WEST_2).build();

		try {
			S3Object object = s3Client.getObject(new GetObjectRequest("csb-motus", key));

			InputStream objectData = object.getObjectContent();
			Transcriptor t = new Transcriptor();
			transcript = "Error!";

			try {
				transcript = t.doTranscription(objectData);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				objectData.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e2) {
			e2.printStackTrace();

		}

		try {
			System.out.println("Adding a new item...");

			PutItemOutcome outcome2 = table
					.putItem(new Item().withPrimaryKey("filename", key).withString("duration", "dtest")
							.withString("status", "Analyzing").withString("transcript", transcript)

			);

			System.out.println("PutItem succeeded:\n" + outcome2.getPutItemResult());

		} catch (Exception e) {
			System.err.println("Unable to add item: " + key);
			System.err.println(e.getMessage());
		}
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
		// runWithPayload("sentiment-analysis",json);
		return transcript;
	}

	public void runWithoutPayload(String functionName) {
		runWithPayload(functionName, null);
	}

	public void runWithPayload(String functionName, String payload) {
		AWSLambdaAsync lambda = AWSLambdaAsyncClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(b)).withRegion(Regions.US_WEST_2).build();
		// lambda.setRegion(Region.getRegion(Regions.US_WEST_2));
		System.err.println("Sending JSON: " + payload);

		InvokeRequest req = new InvokeRequest()

				.withFunctionName(functionName).withPayload(ByteBuffer.wrap(payload.getBytes()));

		Future<InvokeResult> future_res = lambda.invokeAsync(req);
	}

}

package edu.lehigh.csb.motus.app.sentiment;

import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

class SentimentScore{
	String sentence;
	int score;
	
	SentimentScore(String sentence, int score){
		this.sentence = sentence;
		this.score = score;
	}
}

public class Sentiment {
	
	
	public ArrayList<SentimentScore> findSentiment( String line )
    {
       
        Annotation annotation = processLine(line);

         return findMainSentiment(annotation);

      
     }
        
    
	 private ArrayList<SentimentScore> findMainSentiment(Annotation annotation) {

	        int mainSentiment = Integer.MIN_VALUE;	        
	        ArrayList<SentimentScore> result = new ArrayList<SentimentScore>();

	        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	             Tree tree = sentence.get(SentimentAnnotatedTree.class);
	             mainSentiment = RNNCoreAnnotations.getPredictedClass(tree);
	             result.add(new SentimentScore(String.valueOf(sentence),mainSentiment));
	        }
	        return result;

	     }
	 
	   private Annotation processLine(String line) {
	        StanfordCoreNLP pipeline = createPieline();
	        return pipeline.process(line);

	     }

	     private StanfordCoreNLP createPieline() {
	        Properties props = createPipelieProperties();
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	        return pipeline;
	     }
	     
	     private Properties createPipelieProperties() {
	        Properties props = new Properties();
	        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
	        return props;

	     }

}

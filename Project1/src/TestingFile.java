import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import indexcreation.BasicTokenizerBI;
import indexcreation.BasicTokenizerSP;
import indexcreation.DocumentProcessing;
import indexcreation.IndexCreator;
import indexcreation.Posting;
import indexcreation.Tokenizer;
import indexcreation.preprocess.CaseFold;
import indexcreation.preprocess.PreprocessWord;
import indexcreation.preprocess.Stemmer;
import indexcreation.preprocess.StopWords;
import search.TfIdf;

import java.util.LinkedList;

public class TestingFile {

    public static void main(String[] args) throws IOException  {
    	
    	HashMap<String, LinkedList<Posting>> indexMap = null;
    	HashMap<String, Integer>  docLen = null;

   /*    	
        try {        	
        	// get index + doclen
        	String inputDirectoryPath = "C:\\work\\TU Wien\\Advanced Information Retrieval\\output\\";
            
        	FileInputStream streamIn = new FileInputStream(inputDirectoryPath + "index0.sr");
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            
            FileInputStream streamIn2 = new FileInputStream(inputDirectoryPath + "wordFreq.sr");
            ObjectInputStream objectinputstream2 = new ObjectInputStream(streamIn2);
            
            indexMap = (HashMap<String, LinkedList<Posting>>) (objectinputstream.readObject());
            docLen = (HashMap<String, Integer>) (objectinputstream2.readObject());
            
            // query
            String[] query = new String[] {"cancel", "hostag", "wreck", "pick"}; 
            
            // calculate score
            
			int collectionSize = docLen.size();
            double score[] = new double[collectionSize];
            for (int k=0; k<score.length; k++) {
            	score[k] = 0;
            }
            
            // for each query term
            for(int i=0; i<query.length; i++) {
            	// get tfidf for the query
            	
            	
            	// get tfidf for the document
            	LinkedList<Posting> postingsList = indexMap.get(query[i]);
            	if (postingsList != null && !postingsList.isEmpty()) {
            		for (int j=0; j<postingsList.size(); j++) {
            			int tfd = postingsList.get(j).termFreq;
            			String docId = postingsList.get(j).docID;
            			
            			int df = postingsList.size();
            			double tfIdf = TfIdf.getWeight(tfd, df, collectionSize);
            			System.out.println(query[i] + ", " + docId +  ", tfidf=" + tfIdf + ", tfd=" + tfd + ", df=" + df + ", N=" + collectionSize);
            		}
            	}
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
            
*/            
        
        String topicNr = "407";
        final Pattern topicNoPattern = Pattern.compile("<num> Number: " + topicNr);
        final Pattern textStartPattern = Pattern.compile("<title>");
        final Pattern textEndPattern = Pattern.compile("</top>");

        String inputDirectoryPath = "C:\\work\\TU Wien\\Advanced Information Retrieval\\dataset\\TREC8all";
        Path filePath = FileSystems.getDefault().getPath(inputDirectoryPath, "topicsTREC8Adhoc.txt");
        List<String> topicLines = Files.readAllLines(filePath, Charset.forName("ISO8859-1"));

               
        boolean topicFound = false;
        boolean textStarted = false;
        String result = "";
        for (int i=0; i<topicLines.size(); i++) {
        	String line = topicLines.get(i);
        	
            Matcher topicNoMatcher = topicNoPattern.matcher(line);
            Matcher textStartMatcher = textStartPattern.matcher(line);
            Matcher textEndMatcher = textEndPattern.matcher(line);

            if (topicNoMatcher.find()) {
                topicFound = true;
            }

            if (textStartMatcher.find() && topicFound) {
            	textStarted = true;
            }

            if (topicFound == true && textStarted == true) {
                result = result + " " + line;            		
        	}
            
            if (textEndMatcher.find() && textStarted && topicFound) {
            	break;
            }

        }
        
        Tokenizer tokenizer = new BasicTokenizerSP();
        List<PreprocessWord> preprocessWords = new LinkedList<>();
        preprocessWords.add(new StopWords());
        preprocessWords.add(new Stemmer());
        preprocessWords.add(new CaseFold());
        
        DocumentProcessing documentProcessing = new DocumentProcessing(tokenizer, preprocessWords);
        String[] queryTerms = documentProcessing.processLineToTerms(result);
        
        HashMap<String, Integer> queryWordCnt = new HashMap();
        for (int j=0;j<queryTerms.length; j++) {
            //System.out.println(queryTerms[j]);
        	if(queryWordCnt.containsKey(queryTerms[j])) {
        		int count = queryWordCnt.get(queryTerms[j]);
        		queryWordCnt.put(queryTerms[j], count+1);
        	}
        	else {
        		queryWordCnt.put(queryTerms[j], 1);
        	}
        	
        }
        System.out.println(queryTerms.length);

    }
}

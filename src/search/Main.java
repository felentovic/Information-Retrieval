package search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

    public static List<String> getAllTopics() {
    	
    	List<String> topics = new ArrayList<String>(); 
    	topics.add("401");
    	topics.add("402");
    	topics.add("403");
    	topics.add("404");
    	topics.add("405");
    	topics.add("406");
    	topics.add("407");
    	topics.add("408");
    	topics.add("409");
    	topics.add("410");
    	topics.add("411");
    	topics.add("412");
    	topics.add("413");
    	topics.add("414");
    	topics.add("415");
    	topics.add("416");
    	topics.add("417");
    	topics.add("418");
    	topics.add("419");
    	topics.add("420");
    	topics.add("421");
    	topics.add("422");
    	topics.add("423");
    	topics.add("424");
    	topics.add("425");
    	topics.add("426");
    	topics.add("427");
    	topics.add("428");
    	topics.add("429");
    	topics.add("430");
    	topics.add("431");
    	topics.add("432");
    	topics.add("433");
    	topics.add("434");
    	topics.add("435");
    	topics.add("436");
    	topics.add("437");
    	topics.add("438");
    	topics.add("439");
    	topics.add("440");
    	topics.add("441");
    	topics.add("442");
    	topics.add("443");
    	topics.add("444");
    	topics.add("445");
    	topics.add("446");
    	topics.add("447");
    	topics.add("448");
    	topics.add("449");
    	topics.add("450");

    	return topics;
    }
    
    private static CommandLine argumentParsing(String[] args) {
        Options options = new Options();

        Option function = new Option("f", "function", true, "Function: one of TFIDF, BM25, BM25VA ");
        function.setRequired(true);
        options.addOption(function);

        Option k1 = new Option("k1", "k1", true, "Parameter for functions BM25, BM25VA. Default value is 1.2");
        k1.setRequired(false);
        options.addOption(k1);
        
        Option k3 = new Option("k3", "k3", true, "Parameter for functions BM25, BM25VA. Default value is 8");
        k3.setRequired(false);
        options.addOption(k3);
        
        Option b = new Option("b", "b", true, "Parameter for functions BM25, BM25VA. Default value is 0.75");
        b.setRequired(false);
        options.addOption(b);

        Option topicFile = new Option("tf", "topicfile", true, "File with all the topics ");
        topicFile.setRequired(true);
        options.addOption(topicFile);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        return cmd;
    }    
    
    public static void main(String[] args) throws Exception  {
    	
    	List<String> topics = getAllTopics();
    	
        CommandLine cmd = argumentParsing(args);

        String _function = cmd.getOptionValue("f");
        String _k1 = cmd.getOptionValue("k1");    	
        String _k3 = cmd.getOptionValue("k3");    	
        String _b = cmd.getOptionValue("b");    	
        String _topicFile = cmd.getOptionValue("tf");    	
    	
    	String path2Topics = "C:\\work\\TU Wien\\Advanced Information Retrieval\\dataset\\TREC8all";
    	//String topicFileName = "topicsTREC8Adhoc.txt";
    	String topicFileName = _topicFile;
    	String inputPathIndex = "C:\\work\\TU Wien\\Advanced Information Retrieval\\output\\";
    	String outputPathIndex = "C:\\work\\TU Wien\\Advanced Information Retrieval\\output\\";
    	
		// params default values
    	double k1 = 1.2;
    	if (_k1 != null) {
    		k1 = Double.valueOf(_k1.trim()).doubleValue();
    	}
    	
    	// default
    	double k3 = 8;
    	if (_k3 != null) {
    		k3 = Double.valueOf(_k3.trim()).doubleValue();
    	}

    	// default
    	double b = 0.75;    	
    	if (_b != null) {
    		b = Double.valueOf(_b.trim()).doubleValue();
    	}
    	
    	CosineScore cos = new CosineScore(path2Topics, topicFileName, inputPathIndex, outputPathIndex); 
    	for (String topic : topics) {
    		if (_function.trim().compareTo("BM25")==0) {
            	cos.cosineBM25(topic, k1, k3, b);    			
    		}
    		else if (_function.trim().compareTo("BM25VA")==0) {
            	cos.cosineBM25VA(topic, k1, k3);
    		} else if (_function.trim().compareTo("TFIDF")==0) {
            	cos.cosineTfIdf(topic);    	    	
    		} else {
    			System.out.println("Function should be one of: TFIDF, BM25, BM25VA.\nExiting.");
                System.exit(1);
    		}
    	}
    }
    
}


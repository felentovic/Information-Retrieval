package search;

import indexcreation.BasicTokenizerBI;
import indexcreation.BasicTokenizerSP;
import indexcreation.DocumentProcessing;
import indexcreation.Tokenizer;
import indexcreation.preprocess.*;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static List<String> getAllTopics() {

        List<String> topics = new ArrayList<String>();

        for (int i = 401; i <= 450; i++) {
            topics.add(String.valueOf(i));
        }
        
        /*
        //topics.add("401");
        //topics.add("402");
        topics.add("403");
        topics.add("404");
        topics.add("405");
        topics.add("406");
        topics.add("407");
        topics.add("408");
        //topics.add("409");

        //topics.add("416");
        //topics.add("417");
        //topics.add("418");

        topics.add("432");
        //topics.add("433");
        topics.add("434");
        topics.add("438");
        
        topics.add("441");
        //topics.add("442");
        //topics.add("443");
        //topics.add("444");
        topics.add("445");
        //topics.add("446");
        topics.add("447");
        */

        return topics;
    }

    private static CommandLine argumentParsing(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "Directory with documents");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "Index output folder");
        output.setRequired(true);
        options.addOption(output);

        Option topicsInput = new Option("ti", "topicsinput", true, "Topics input folder");
        topicsInput.setRequired(true);
        options.addOption(topicsInput);

        Option stemmer = new Option("s", "stemming", false, "Use of stemming ");
        stemmer.setRequired(false);
        options.addOption(stemmer);

        Option lemmatization = new Option("", "lemmatization", false, "Use of lemmatization ");
        lemmatization.setRequired(false);
        options.addOption(lemmatization);

        Option caseFold = new Option("c", "casefold", false, "Use of casefold ");
        caseFold.setRequired(false);
        options.addOption(caseFold);

        Option normalization = new Option("n", "normalization", false, "Use of normalization ");
        normalization.setRequired(false);
        options.addOption(normalization);


        Option stopWords = new Option("w", "stopwords", false, "Use of stop words removal ");
        stopWords.setRequired(false);
        options.addOption(stopWords);

        Option tokenizer = new Option("t", "tokenizer", true, "Tokenizer type. BI for BreakIterator and SP for split");
        tokenizer.setRequired(false);
        options.addOption(tokenizer);

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

    public static void main(String[] args) throws Exception {

        List<String> topics = getAllTopics();

        CommandLine cmd = argumentParsing(args);

        String _function = cmd.getOptionValue("f");
        String _k1 = cmd.getOptionValue("k1");
        String _k3 = cmd.getOptionValue("k3");
        String _b = cmd.getOptionValue("b");
        String _topicFile = cmd.getOptionValue("tf");

        String inputPathIndex = cmd.getOptionValue("input");
        String outputPathIndex = cmd.getOptionValue("output");
        String path2Topics = cmd.getOptionValue("topicsinput");

        List<PreprocessWord> preprocessWords = new LinkedList<>();

        if (cmd.hasOption("stopwords")) {
            preprocessWords.add(new StopWords());
        }

        if (cmd.hasOption("normalization")) {
            preprocessWords.add(new Normalizer());
        }

        if (cmd.hasOption("stemming")) {
            preprocessWords.add(new Stemmer());
        }

        if (cmd.hasOption("lemmatization")) {
            preprocessWords.add(new Lemmatizer());
        }
        if (cmd.hasOption("casefold")) {
            preprocessWords.add(new CaseFold());
        }


        Tokenizer tokenizer;
        if (cmd.getOptionValue("tokenizer").equals("BI")) {
            tokenizer = new BasicTokenizerBI();
        } else {
            tokenizer = new BasicTokenizerSP();
        }
        //String topicFileName = "topicsTREC8Adhoc.txt";
        String topicFileName = _topicFile;

        // params default values
        final double k1;
        if (_k1 != null) {
            k1 = Double.valueOf(_k1.trim()).doubleValue();
        } else {
        	k1 = 1.2;
        }

        // default
        final double k3;
        if (_k3 != null) {
            k3 = Double.valueOf(_k3.trim()).doubleValue();
        } else {
        	k3 = 8;
        }

        // default
        final double b;
        if (_b != null) {
            b = Double.valueOf(_b.trim()).doubleValue();
        }
        else {
        	b = 0.75;
        }

        DocumentProcessing documentProcessing = new DocumentProcessing(tokenizer, preprocessWords);
        CosineScore cos = new CosineScore(documentProcessing, path2Topics, topicFileName, inputPathIndex, outputPathIndex);
        
        /*
        for (String topic : topics) {
            if (_function.trim().compareTo("BM25") == 0) {
            	System.out.println("BM25");
                //cos.cosineTfIdf(topic);
                cos.cosineBM25(topic, k1, k3, b);
                //cos.cosineBM25VA(topic, k1, k3);
            } else if (_function.trim().compareTo("BM25VA") == 0) {
            	System.out.println("BM25VA");
                //cos.cosineTfIdf(topic);
                //cos.cosineBM25(topic, k1, k3, b);
                cos.cosineBM25VA(topic, k1, k3);
            } else if (_function.trim().compareTo("TFIDF") == 0) {
            	System.out.println("TFIDF");
                cos.cosineTfIdf(topic);
                //cos.cosineBM25(topic, k1, k3, b);
                //cos.cosineBM25VA(topic, k1, k3);
            } else {
                System.out.println("Function should be one of: TFIDF, BM25, BM25VA.\nExiting.");
                System.exit(1);
            }
        }
        */
        
            topics.stream().parallel().forEach(topic-> { if (_function.trim().compareTo("BM25") == 0) {
                try {
					cos.cosineBM25(topic, k1, k3, b);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } else if (_function.trim().compareTo("BM25VA") == 0) {
                try {
					cos.cosineBM25VA(topic, k1, k3);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } else if (_function.trim().compareTo("TFIDF") == 0) {
                try {
					cos.cosineTfIdf(topic);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } else {
                System.out.println("Function should be one of: TFIDF, BM25, BM25VA.\nExiting.");
                System.exit(1);
            }});       
            
            System.out.println("files written to output folder...");
    }

}


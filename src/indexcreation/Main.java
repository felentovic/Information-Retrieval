package indexcreation;

import indexcreation.preprocess.*;
import indexcreation.preprocess.StopWords;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {

        // String filesPath = "/home/felentovic/Documents/TUWien/Semester_4/Advanced_Information_Retrieval/Excercise1/documents/Adhoc/fbis";
//        String indexPath = "/home/felentovic/tmp";

        CommandLine cmd = argumentParsing(args);

        String inputDirectoryPath = cmd.getOptionValue("input");
        String outputDirectoryPath = cmd.getOptionValue("output");

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

        int indexSize;
        try {
            indexSize = Integer.parseInt(cmd.getOptionValue("indexsize", "500000"));
        } catch (NumberFormatException e) {
            indexSize = 500000;
        }


        DocumentProcessing documentProcessing = new DocumentProcessing(tokenizer, preprocessWords);
        IndexCreator indexCreator = new IndexCreator(documentProcessing, outputDirectoryPath, indexSize);

        Parser parser = new Parser(indexCreator);
        List<Path> files = Files.walk(Paths.get(inputDirectoryPath)).filter(Files::isRegularFile).collect(Collectors.toList());

        int counter = 1;
        for (Path file : files) {
            System.out.println("Parsing " + counter++ + ". file:" + file.getFileName());
            parser.parse(file);
        }

        parser.close();

    }

    private static CommandLine argumentParsing(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "Directory with documents");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "Index output folder");
        output.setRequired(true);
        options.addOption(output);

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


        Option indexSize = new Option("is", "indexsize", true, "Number of terms in one index ");
        indexSize.setRequired(false);
        options.addOption(indexSize);


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

}

package indexcreation;

import indexcreation.preprocess.CaseFold;
import indexcreation.preprocess.Normalizer;
import indexcreation.preprocess.PreprocessWord;
import indexcreation.preprocess.Stemmer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        String filesPath = "/home/felentovic/Documents/TUWien/Semester_4/Advanced_Information_Retrieval/Excercise1/documents/Adhoc/fbis";
        String indexPath = "/home/felentovic/tmp";
        int indexSize = 300000;
        PreprocessWord stemmer = new Stemmer();
        PreprocessWord caseFold = new CaseFold();
        PreprocessWord normalizer = new Normalizer();

        Tokenizer tokenizer = new BasicTokenizerSP();
        DocumentProcessing documentProcessing = new DocumentProcessing(tokenizer, Arrays.asList(normalizer, stemmer, caseFold));
        IndexCreator indexCreator = new IndexCreator(documentProcessing, indexPath, indexSize);

        Parser parser = new Parser(indexCreator);
        List<Path> files = Files.walk(Paths.get(filesPath)).filter(Files::isRegularFile).collect(Collectors.toList());

        int counter = 1;
        for (Path file : files) {
            System.out.println("Parsing "+counter+++". file:"+ file.getFileName());
            parser.parse(file);
        }

        parser.close();
    }

}

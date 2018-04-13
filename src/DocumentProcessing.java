import org.apache.lucene.analysis.core.StopAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class DocumentProcessing {

    private Tokenizer tokenizer;
    private List<PreprocessWord> preprocessWords;
    private Stream<String> fileStream;

    public DocumentProcessing(Tokenizer tokenizer, List<PreprocessWord> preprocessWords) {
        this.tokenizer = tokenizer;
        this.preprocessWords = preprocessWords;
    }

    private void setTextStream(Stream<String> fileStream){
        this.fileStream = fileStream;
    }

    public Stream<String[]> getTermStream() {
            return fileStream.map(this::processLineToTerms);

    }

    private String[] processLineToTerms(String line){
        List<String> terms = new LinkedList<>();

        String[] tokens = tokenizer.tokenize(line);
        for (String token : tokens) {
            String processedToken = preprocessWordComposite(token);

            if (!isStopWord(processedToken)) {
                terms.add(processedToken);
            }
        }
        return terms.toArray(new String[terms.size()]);
    }

    private boolean isStopWord(String word) {
        //match tags <TAG> <\TAG>
        return word.length() == 0 || StopWords.isStopWord(word) || word.matches("\\<\\\\?[A-z]+\\>");
    }

    private String preprocessWordComposite(String token) {
        String word = token;
        for(PreprocessWord preprocessWord : preprocessWords){
            word = preprocessWord.apply(word);
        }
        return word;
    }


}

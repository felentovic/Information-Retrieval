package indexcreation;

import indexcreation.preprocess.PreprocessWord;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class DocumentProcessing {

    private Tokenizer tokenizer;
    private Iterable<PreprocessWord> preprocessWords;
    private Stream<String> fileStream;

    public DocumentProcessing(Tokenizer tokenizer, Iterable<PreprocessWord> preprocessWords) {
        this.tokenizer = tokenizer;
        this.preprocessWords = preprocessWords;
    }

    public void setTextStream(Stream<String> fileStream) {
        this.fileStream = fileStream;
    }

    public Stream<String[]> getTermStream() {
        return fileStream.map(this::processLineToTerms);

    }

    public String[] processLineToTerms(String line) {
        List<String> terms = new LinkedList<>();

        String[] tokens = tokenizer.tokenize(line);
        for (String token : tokens) {
            String processedToken = preprocessWordComposite(token);

            if (!notWord(processedToken)) {
                terms.add(processedToken);
            }
        }
        return terms.toArray(new String[terms.size()]);
    }

    private boolean notWord(String word) {
        //match tags <TAG> <\TAG>
        return word.length() == 0 || SpecialCharacter.isSpecialChar(word);
    }

    private String preprocessWordComposite(String token) {
        String word = token;
        for (PreprocessWord preprocessWord : preprocessWords) {
            word = preprocessWord.apply(word);
        }
        return word;
    }


}

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;

public class BasicTokenizer implements Tokenizer{

    private BreakIterator tokenizer = BreakIterator.getWordInstance();

    public String[] tokenize(String source) {
        List<String> tokens = new LinkedList<>();
        tokenizer.setText(source);
        for (int end = tokenizer.next(), start = tokenizer.first(); end != BreakIterator.DONE; start = end, end = tokenizer.next()) {
            tokens.add(source.substring(start, end));
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    
    
}

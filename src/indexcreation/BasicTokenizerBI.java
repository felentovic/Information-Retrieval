package indexcreation;

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;

public class BasicTokenizerBI implements Tokenizer {

    private BreakIterator tokenizer = BreakIterator.getWordInstance();

    public String[] tokenize(String source) {
        List<String> tokens = new LinkedList<>();
        //remove tags if there are some
        String preprocessedSrc = source.replaceAll("</?[A-z]+>", "");
        tokenizer.setText(preprocessedSrc);
        for (int end = tokenizer.next(), start = tokenizer.first(); end != BreakIterator.DONE; start = end, end = tokenizer.next()) {
            tokens.add(preprocessedSrc.substring(start, end));
        }

        return tokens.toArray(new String[tokens.size()]);
    }


}

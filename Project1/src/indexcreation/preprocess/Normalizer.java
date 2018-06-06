package indexcreation.preprocess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Normalizer implements PreprocessWord {
    private Pattern numberPattern;
    public Normalizer(){
        numberPattern = Pattern.compile("\\d+.?\\d*");

    }

    @Override
    public String apply(String word) {
        Matcher numberMatcher = numberPattern.matcher(word);

        if (numberMatcher.find()) {
            return word;

        }else{
            return word.replaceAll("\\.", "");
        }
    }
}

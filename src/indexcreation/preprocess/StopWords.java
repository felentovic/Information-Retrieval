package indexcreation.preprocess;

import java.util.Arrays;
import java.util.HashSet;

public class StopWords implements PreprocessWord {



    private final String[] SET_VALUES = new String[]{"a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "such",
            "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with"};
    private HashSet<String> stopWords = new HashSet<>(Arrays.asList(SET_VALUES));

    @Override
    public String apply(String word) {
        if(stopWords.contains(word)){
            return "";
        }else{
            return word;
        }
    }
}

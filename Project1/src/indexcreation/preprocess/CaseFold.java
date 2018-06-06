package indexcreation.preprocess;

import indexcreation.preprocess.PreprocessWord;

public class CaseFold implements PreprocessWord {
    @Override
    public String apply(String word) {
        return word.toLowerCase();

    }
}

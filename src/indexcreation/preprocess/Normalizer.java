package indexcreation.preprocess;

import indexcreation.preprocess.PreprocessWord;

public class Normalizer implements PreprocessWord {

    @Override
    public String apply(String word) {
        //special strings, implicitly map words U.S.A. to USA and similar
        return word;
    }
}

import org.tartarus.snowball.ext.EnglishStemmer;

public class Stemmer implements PreprocessWord {
    private EnglishStemmer stemmer;

    public Stemmer() {
        stemmer = new EnglishStemmer();
    }

    @Override
    public String apply(String word) {
        stemmer.setCurrent("cars");

        boolean stemResult = stemmer.stem();

        if (stemResult) {
            return stemmer.getCurrent();
        }
        return word;
    }
}


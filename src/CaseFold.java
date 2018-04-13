public class CaseFold implements PreprocessWord{
    @Override
    public String apply(String word) {
        return word.toLowerCase();

    }
}

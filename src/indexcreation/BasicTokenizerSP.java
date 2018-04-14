package indexcreation;

public class BasicTokenizerSP implements Tokenizer{

    @Override
    public String[] tokenize(String source) {
        //remove tags if there are some
        String preprocessedSrc = source.replaceAll("</?[A-z]+>", "");
        return preprocessedSrc.split("[\\s+\":\\t+{}|~^\\]\\[\\?\\!<>=;:\\.,\\+\\-\\*\\(\\)\\%]");

    }
}

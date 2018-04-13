import java.text.BreakIterator;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {

        String str = "\"this is a test. \"Live\" for U.S.A. Tocqueville's. I am pro-\nnounciation in the USA";
        //after having tokens, just remove . from
//        StringTokenizer tokenizer = new StringTokenizer(str, " \t\n\r\f,:;?![]'\"");
//        while (tokenizer.hasMoreTokens()) {
//            System.out.println(tokenizer.nextToken());
//        }


        String stringToExamine = str;
        //print each word in order
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(stringToExamine);
        printEachForward(boundary, stringToExamine);

    }

    public static void printEachForward(BreakIterator boundary, String source) {
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            System.out.println(source.substring(start, end));
        }
    }

}

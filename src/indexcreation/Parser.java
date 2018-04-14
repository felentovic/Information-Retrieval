package indexcreation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Parser {

    private IndexCreator indexCreator;

    public Parser(IndexCreator indexCreator) {
        this.indexCreator = indexCreator;
    }

    public void parse(Path filePath) throws IOException {

        final Pattern docnoPattern = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
        final Pattern textStartPattern = Pattern.compile("<TEXT>");
        final Pattern textEndPattern = Pattern.compile("</TEXT>");


        Stream<String> linesStream = Files.lines(filePath, Charset.forName("ISO8859-1"));

        linesStream.forEachOrdered(new Consumer<String>() {
            int currentLine = 0;
            int textStart = 0;
            int textEnd = 0;
            String docID = null;
            boolean paired = false;
            @Override
            public void accept(String line) {
                currentLine++;
                Matcher docIDMatcher = docnoPattern.matcher(line);
                if (docIDMatcher.find()) {
                    if(!paired && docID != null){
                        System.out.println("No text found " + docID);
                    }
                    docID = docIDMatcher.group(1).trim();
                    paired = false;
                }

                Matcher textStartMatcher = textStartPattern.matcher(line);
                if (textStartMatcher.find()) {
                    textStart = currentLine;
                }

                Matcher textEndMatcher = textEndPattern.matcher(line);
                if (textEndMatcher.find()) {

                    try (Stream<String> indexStream = Files.lines(filePath, Charset.forName("ISO8859-1"));) {
                        textEnd = currentLine;
                        indexCreator.spimi(docID, indexStream.skip(textStart).limit(textEnd - textStart));
                        textStart = textEnd = -1;
                        paired = true;
                    } catch (IOException e) {
                    }

                }
            }
        });
        linesStream.close();

    }

    public void close() throws IOException {
        indexCreator.dumpIndexToFile();
        indexCreator.dumpWordFreqToFile();
    }
}

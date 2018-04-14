package indexcreation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class IndexCreator {
    private HashMap<String, LinkedList<Posting>> index;
    private String indexPath;
    private DocumentProcessing documentProcessing;
    private int indexSize;
    private int indexCounter;
    private Map<String, Integer> docIdWordCnt;

    public IndexCreator(DocumentProcessing documentProcessing, String indexFolderPath, int indexSize) {
        this.index = new HashMap<>();
        this.docIdWordCnt= new HashMap<>();
        this.indexPath = indexFolderPath;
        this.documentProcessing = documentProcessing;
        this.indexSize = indexSize;
        this.indexCounter = 0;
    }

    public void spimi(String docId, Stream<String> textStream) {
        documentProcessing.setTextStream(textStream);
        Stream<String[]> termsStream = documentProcessing.getTermStream();

        AtomicInteger wordCount = new AtomicInteger();
        termsStream.forEach(terms -> {for (String term : terms) {
                wordCount.addAndGet(terms.length);
                addToIndex(docId, term);
        }
        });
        //save wordcount
        docIdWordCnt.put(docId,wordCount.get());
    }

    public void dumpIndexToFile() throws IOException {
        FileOutputStream fout = new FileOutputStream(String.valueOf(Paths.get(indexPath, "index"+indexCounter+".sr")));
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(index);
        indexCounter++;
        index.clear();
    }

    public void dumpWordFreqToFile() throws IOException {
        FileOutputStream  fout = new FileOutputStream(String.valueOf(Paths.get(indexPath, "wordFreq.sr")));
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(docIdWordCnt);
    }

    private void addToIndex(String docId, String term)  {
        if (index.size() + 1 > indexSize) {
            try {
                dumpIndexToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LinkedList<Posting> postingList;
        if (!index.containsKey(term)) {
            postingList = new LinkedList<>();
            index.put(term, postingList);
        } else {
            postingList = index.get(term);
        }



        if (postingList.isEmpty() || !postingList.peekLast().docID.equals(docId)) {
            postingList.add(new Posting(docId));
        }else{
            postingList.peekLast().termFreq++;
        }


    }
}

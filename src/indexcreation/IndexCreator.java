package indexcreation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class IndexCreator {
    private TreeMap<String, LinkedList<Posting>> index;
    private String indexPath;
    private DocumentProcessing documentProcessing;
    private int indexSize;
    private int indexCounter;
    private Map<String, Integer> docIdWordCnt;
    private Map<String, Integer> docIdTermCnt;
    private Set<String> termSet;

    public IndexCreator(DocumentProcessing documentProcessing, String indexFolderPath, int indexSize) {
        this.index = new TreeMap<>();
        this.docIdWordCnt= new HashMap<>();
        this.docIdTermCnt= new HashMap<>();
        this.termSet = new HashSet<>();
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
                termSet.add(term);
        }
        });
        //save wordcount
        docIdWordCnt.put(docId,wordCount.get());
        docIdTermCnt.put(docId, termSet.size());
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

    public void dumpTermFreqToFile() throws IOException {
        FileOutputStream  fout = new FileOutputStream(String.valueOf(Paths.get(indexPath, "termFreq.sr")));
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(docIdTermCnt);
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
            postingList.sort(new Comparator<Posting>() {
    			@Override
    			public int compare(Posting p1, Posting p2) {
    				return p1.compareTo(p2);
    			}
    		});
        }else{
            postingList.peekLast().termFreq++;
            postingList.sort(new Comparator<Posting>() {
    			@Override
    			public int compare(Posting p1, Posting p2) {
    				return p1.compareTo(p2);
    			}
    		});
        }


    }
}

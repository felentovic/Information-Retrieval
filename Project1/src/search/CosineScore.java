package search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import indexcreation.BasicTokenizerSP;
import indexcreation.DocumentProcessing;
import indexcreation.Posting;
import indexcreation.Tokenizer;
import indexcreation.preprocess.CaseFold;
import indexcreation.preprocess.PreprocessWord;
import indexcreation.preprocess.Stemmer;
import indexcreation.preprocess.StopWords;

public class CosineScore {

	private Path   topicFilePath;
	private String inputPathTopics;
	private String inputPathIndex;
	private String outputPathIndex;
	private FileInputStream streamInIndex;
	private FileInputStream streamInWordFreq;
	private FileInputStream streamIntermFreq;
	private ObjectInputStream objectinputstream;
	private ObjectInputStream objectStreamInWordFreq;
	private ObjectInputStream objectStreamIntermFreq;
	private HashMap<String, LinkedList<Posting>> indexMap;
	private HashMap<String, Integer> docLen;
	private HashMap<String, Integer> termLen;
	private DocumentProcessing documentProcessing;
	private HashMap<String,   HashMap<String, Integer>> queryTermsMap;

	@SuppressWarnings("unchecked")
	public CosineScore(DocumentProcessing documentProcessing, String inputPathTopics, String topicFileName, String inputPathIndex, String outputPathIndex) throws Exception {
		this.documentProcessing = documentProcessing;
		this.inputPathTopics = inputPathTopics;
		this.topicFilePath = FileSystems.getDefault().getPath(this.inputPathTopics, topicFileName);
		this.inputPathIndex = inputPathIndex;
		this.outputPathIndex = outputPathIndex;

		this.streamInWordFreq = new FileInputStream(this.outputPathIndex + "\\wordFreq.sr");
		this.objectStreamInWordFreq = new ObjectInputStream(this.streamInWordFreq);

		this.streamIntermFreq = new FileInputStream(this.outputPathIndex + "\\termFreq.sr");
		this.objectStreamIntermFreq = new ObjectInputStream(this.streamIntermFreq);

		this.indexMap = retrieveIndexFromFiles(this.outputPathIndex);
		this.docLen = (HashMap<String, Integer>) (objectStreamInWordFreq.readObject());
		this.termLen = (HashMap<String, Integer>) (objectStreamIntermFreq.readObject());
		
		this.queryTermsMap = getQueryTermsMap();
		
		this.streamInWordFreq.close();
		this.objectStreamInWordFreq.close();
		this.streamIntermFreq.close();
		this.objectStreamIntermFreq.close();
	}


	@SuppressWarnings("unchecked")
	public LinkedList<Posting> mergePostingLists(LinkedList<Posting> list1, LinkedList<Posting> list2) {
		//System.out.println("mergePostingLists, list1.len=" + list1.size() + ", list2.len=" + list2.size());
		
		LinkedList<Posting> res = new LinkedList<Posting>();

		int i = 0;
		int j = 0;
		while (i<list1.size() && j<list2.size()){
			if (list1.get(i).compareTo(list2.get(j)) < 0) {
				res.add(list1.get(i));
				i++;
			}
			else if (list1.get(i).compareTo(list2.get(j)) > 0) {
				res.add(list2.get(j));
				j++;
			}
			else  {
				// (list1.get(i).compareTo(list2.get(j)) == 0)				
				String docID = list1.get(i).docID;
				Integer termFreq = list1.get(i).termFreq + list2.get(j).termFreq;
				Posting p = new Posting(docID, termFreq);
				res.add(p);
				i++;
				j++;
			}
		}
		if(i < list1.size()){
			res.addAll(list1.subList(i, list1.size()));
		}
		if(j < list2.size()){
			res.addAll(list2.subList(j, list2.size()));			
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, LinkedList<Posting>> retrieveIndexFromFiles(String path) throws Exception{
		//System.out.println("retrieveIndexFromFiles");
		
		List<Path> files = Files.walk(Paths.get(path)).filter(Files::isRegularFile).filter(p -> p.toFile().getName().startsWith("index")).collect(Collectors.toList());

		ObjectInputStream ois;
		HashMap<String, LinkedList<Posting>> openedMap = new HashMap<String, LinkedList<Posting>>();
		//HashMap<String, LinkedList<Posting>> index = new HashMap<String, LinkedList<Posting>>((int) 1700000);
		HashMap<String, LinkedList<Posting>> index = new HashMap<String, LinkedList<Posting>>();
		
		// open streams and get indexes
		int counter = 1;
		for (Path file : files) {
			System.out.println("merging file " + counter + " (" + file.getFileName() + ") / " + files.size());
			ois = new ObjectInputStream(new FileInputStream(file.toFile()));
			openedMap = (HashMap<String, LinkedList<Posting>>) (ois.readObject());

			// start with first
			int keyCounter = 1;
			for (String key : openedMap.keySet()) {
				//if (keyCounter % 1000 == 0)  {
				//	System.out.println(file.getFileName() + ", key " + keyCounter + " / "  + openedMap.keySet().size());					
				//}
				
				if (!index.containsKey(key)) {
					index.put(key, openedMap.get(key));
				}
				else {
					LinkedList<Posting> list1 = index.get(key);
					LinkedList<Posting> list2 = openedMap.get(key);

					LinkedList<Posting> mergedList = mergePostingLists(list1, list2);
					list1.clear();
					list2.clear();
					index.put(key, mergedList);
				}
				
				keyCounter++;
			}
			ois.close();
			openedMap.clear();
			counter++;
		}
		
		// stream to whole index
		FileOutputStream  fout = new FileOutputStream(outputPathIndex + "index_whole.sr");
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(index);
		oos.close();

		/*
		ois = new ObjectInputStream(new FileInputStream(new File("C:\\work\\privat\\AIR2018\\demoout\\index0.sr")));
		index = (HashMap<String, LinkedList<Posting>>) (ois.readObject());
		ois.close();
		System.out.println("index loaded at " + new Date());
		printFreeHeap("index");
		*/
		
		System.out.println("index loaded at " + new Date());

		return index;
	}

    public static List<String> getAllTopics() {
        List<String> topics = new ArrayList<String>();
        for (int i = 401; i <= 450; i++) {
            topics.add(String.valueOf(i));
        }
        return topics;
    }	
	
	public HashMap<String,  HashMap<String, Integer>> getQueryTermsMap() throws IOException {
		HashMap<String,   HashMap<String, Integer>> queryTermsMap = new HashMap<String,   HashMap<String, Integer>>(50);
		
		for (String topic : getAllTopics()) {
			HashMap<String, Integer> queryTerms = getQueryTerms(topic);
			queryTermsMap.put(topic, queryTerms);
		}
		
		return queryTermsMap;
	}
	
	public HashMap<String, Integer> getQueryTerms(String topicNr) throws IOException {
		//System.out.println("getQueryTerms");

		List<String> topicLines = Files.readAllLines(this.topicFilePath, Charset.forName("ISO8859-1"));

		final Pattern topicNoPattern = Pattern.compile("<num> Number: " + topicNr);
		final Pattern textStartPattern = Pattern.compile("<title>");
		final Pattern textEndPattern = Pattern.compile("</top>");


		boolean topicFound = false;
		boolean textStarted = false;
		String result = "";
		for (int i=0; i<topicLines.size(); i++) {
			String line = topicLines.get(i);

			Matcher topicNoMatcher = topicNoPattern.matcher(line);
			Matcher textStartMatcher = textStartPattern.matcher(line);
			Matcher textEndMatcher = textEndPattern.matcher(line);

			if (topicNoMatcher.find()) {
				topicFound = true;
			}

			if (textStartMatcher.find() && topicFound) {
				textStarted = true;
			}

			if (topicFound == true && textStarted == true) {
				result = result + " " + line;
			}

			if (textEndMatcher.find() && textStarted && topicFound) {
				break;
			}

		}

		String[] queryTerms = documentProcessing.processLineToTerms(result);

		HashMap<String, Integer> queryWordCnt = new HashMap<String, Integer>(200);
		for (int j=0;j<queryTerms.length; j++) {
			if(queryWordCnt.containsKey(queryTerms[j])) {
				int count = queryWordCnt.get(queryTerms[j]);
				queryWordCnt.put(queryTerms[j], count+1);
			}
			else {
				queryWordCnt.put(queryTerms[j], 1);
			}
		}

		return queryWordCnt;
	}

	public void cosineTfIdf(String topic) throws Exception  {
		//System.out.println("cosineTfIdf " + topic);

		// query
		HashMap<String, Integer> queryTerms = queryTermsMap.get(topic); //getQueryTerms(topic);

		int collectionSize = docLen.size();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>((int) 600000);

		// for each query term
		int counter = 0;
		for(String term : queryTerms.keySet()) {			
			//System.out.println(new Date() + ": processing term " + term + ", " + counter + " / " + queryTerms.size());
			// get tfidf for the document
			LinkedList<Posting> postingsList = indexMap.get(term);
			if (postingsList != null && !postingsList.isEmpty()) {
				//System.out.println(new Date() + ": postingslist.size = " + postingsList.size());

				int df = postingsList.size();
				int qtf = queryTerms.get(term);
				double wtq = TfIdf.getWeight(qtf, df, collectionSize);
				for (int j=0; j<postingsList.size(); j++) 
				{
					int tfd = postingsList.get(j).termFreq;
					String docId = postingsList.get(j).docID;

					// get tfidf for the query
					double wtd = TfIdf.getWeight(tfd, df, collectionSize);

					if (scoreMap.containsKey(docId)) {
						double score = scoreMap.get(docId);
						score = score + (wtq*wtd);
						scoreMap.put(docId, score);
					}
					else {
						scoreMap.put(docId, wtq*wtd);
					}
				}
			}
			counter++;
		}

		// divide by doc length
		//System.out.println(new Date() + ": divide by doclength ");
		int mycounter = 0;
		for (String docId : scoreMap.keySet()) {
			//System.out.println(new Date() + ": counter " + mycounter + " / " + scoreMap.keySet().size());
			double score =  scoreMap.get(docId) / (double) docLen.get(docId);
			scoreMap.put(docId, score);
			mycounter++;
		}

		printTopScore2File(scoreMap, topic);
		scoreMap.clear();
		queryTerms.clear();
	}

	public void cosineBM25(String topic, double k1, double k3, double b) throws Exception  {

		// calc avg len
		double sumLen = 0;
		for (String doc : docLen.keySet()) {
			sumLen = sumLen + docLen.get(doc);
		}
		double avgld = sumLen / (double) docLen.size();

		// query
		HashMap<String, Integer> queryTerms = queryTermsMap.get(topic); //getQueryTerms(topic);

		int collectionSize = docLen.size();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>((int) 600000);

		// for each query term
		for(String term : queryTerms.keySet()) {
			// get bm25 for the document
			LinkedList<Posting> postingsList = indexMap.get(term);
			int tfq = queryTerms.get(term);
			if (postingsList != null && !postingsList.isEmpty()) {
				int df = postingsList.size();
				for (int j=0; j<postingsList.size(); j++) {
					
					String docId = postingsList.get(j).docID;
					int tfd = postingsList.get(j).termFreq;
					int ld = docLen.get(docId);
					BM25 bm25 = new BM25(k1, k3, b);

					// get bm25 for the query
					double w = bm25.getWeight(tfd, tfq, df, ld, avgld, collectionSize);

					if (scoreMap.containsKey(docId)) {
						double score = scoreMap.get(docId);
						score = score + w;
						scoreMap.put(docId, score);
					}
					else {
						scoreMap.put(docId, w);
					}
				}
			}
		}

		printTopScore2File(scoreMap, topic);
		scoreMap.clear();
		queryTerms.clear();		
	}

	public void cosineBM25VA(String topic, double k1, double k3) throws Exception  {

		// calc avg len
		double sumLen = 0.0;
		for (String doc : docLen.keySet()) {
			sumLen = sumLen + docLen.get(doc);
		}
		double avgld = (double) ((double) sumLen / (double) docLen.size());

		double sumAvgtf = 0.0;
		for (String doc : termLen.keySet()) {
			double avgtf = (double) ((double) docLen.get(doc)) / ((double) termLen.get(doc));
			if (Double.isNaN(avgtf)) {
				avgtf = 0.0;
			}
			sumAvgtf = sumAvgtf + avgtf;			
		}
		double mavgtf = (double) ((double) sumAvgtf / (double) docLen.size());

		// query
		HashMap<String, Integer> queryTerms = queryTermsMap.get(topic); //getQueryTerms(topic);

		// calculate score
		int collectionSize = docLen.size();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>((int) 600000);


		// for each query term
		for(String term : queryTerms.keySet()) {
			// get bm25va for the document
			LinkedList<Posting> postingsList = indexMap.get(term);
			int tfq = queryTerms.get(term);
			if (postingsList != null && !postingsList.isEmpty()) {
				int df = postingsList.size();
				for (int j=0; j<postingsList.size(); j++) {
					
					int tfd = postingsList.get(j).termFreq;
					String docId = postingsList.get(j).docID;
					int ld = docLen.get(docId);
					int td = termLen.get(docId);

					BM25VA bm25va = new BM25VA(k1, k3);

					// get bm25va for the query
					double w = bm25va.getWeight(tfd, tfq, df, ld, td, avgld, mavgtf, collectionSize);
					
					if (scoreMap.containsKey(docId)) {
						double score = scoreMap.get(docId);
						score = score + w;
						scoreMap.put(docId, score);
					}
					else {
						scoreMap.put(docId, w);
					}
				}
			}
		}

		printTopScore2File(scoreMap, topic);
		scoreMap.clear();
		queryTerms.clear();		
	}

	public void printTopScore(HashMap<String, Double> scoreMap, String topic) {

		int N = 1000;
		if (N > scoreMap.size()) {
			N = scoreMap.size();
		}

		TreeMap<Double, String> treeMap = new TreeMap<>(Collections.reverseOrder());

		for (String docId : scoreMap.keySet()) {
			if (treeMap.size() < N) {
				treeMap.put(scoreMap.get(docId), docId);
			}
			else {
				double score = scoreMap.get(docId);
				double lowest =treeMap.firstKey();
				if (score >= lowest) {
					treeMap.remove(lowest);
					treeMap.put(score, docId);
				}
			}
		}

		int rank = 1;
		String runname = "grp04-exp1";
		for (double docScore : treeMap.keySet()) {
			System.out.println(topic + " Q0 " + treeMap.get(docScore) + " " + rank + " " + docScore + " " + runname);
			rank++;
		}
	}

	public void printTopScore2File(HashMap<String, Double> scoreMap, String topic) throws Exception {

		int N = 1000;
		if (N >scoreMap.size()) {
			N = scoreMap.size();
		}

		TreeMap<Double, String> treeMap = new TreeMap<>(Collections.reverseOrder());

		for (String docId : scoreMap.keySet()) {
			if (treeMap.size() < N) {
				treeMap.put(scoreMap.get(docId), docId);
			}
			else {
				double score = scoreMap.get(docId);
				double lowest =treeMap.firstKey();
				if (score >= lowest) {
					treeMap.remove(lowest);
					treeMap.put(score, docId);
				}
			}
		}

		int rank = 1;
		String runname = "grp04-exp1";
        FileOutputStream fout = new FileOutputStream(String.valueOf(Paths.get(outputPathIndex, "result-"+topic+".top")));
		for (double docScore : treeMap.keySet()) {
			String line = topic + " Q0 " + treeMap.get(docScore) + " " + rank + " " + docScore + " " + runname + "\n";
	        fout.write(line.getBytes());
			rank++;
		}
        
		fout.close();			
	}
		
	public HashMap<String, LinkedList<Posting>> getIndexMap() {
		return indexMap;
	}

	public HashMap<String, Integer> getDocLenMap() {
		return docLen;
	}

	public HashMap<String, Integer> getTermLenMap() {
		return termLen;
	}
	
	public void printFreeHeap(String pos) {
		int mb = 1024*1024;
		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();
		System.out.println("##### Heap utilization statistics [MB] #####");
		//Print used memory
		System.out.println("Used Memory(" + pos + "): " 
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		//Print free memory
		System.out.println("Free Memory(" + pos + "): "
			+ runtime.freeMemory() / mb);
		//Print total available memory
		System.out.println("Total Memory(" + pos + "): " + runtime.totalMemory() / mb);
		//Print Maximum available memory
		System.out.println("Max Memory(" + pos + "): " + runtime.maxMemory() / mb);				
	}
}
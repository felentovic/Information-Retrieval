package search;

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

	@SuppressWarnings("unchecked")
	public CosineScore(String inputPathTopics, String topicFileName, String inputPathIndex, String outputPathIndex) throws Exception {
		this.inputPathTopics = inputPathTopics;
		this.topicFilePath = FileSystems.getDefault().getPath(this.inputPathTopics, topicFileName);
		this.inputPathIndex = inputPathIndex;
		this.outputPathIndex = outputPathIndex;

		this.streamInWordFreq = new FileInputStream(this.inputPathIndex + "wordFreq.sr");
		this.objectStreamInWordFreq = new ObjectInputStream(this.streamInWordFreq);

		this.streamIntermFreq = new FileInputStream(this.inputPathIndex + "termFreq.sr");
		this.objectStreamIntermFreq = new ObjectInputStream(this.streamIntermFreq);

		this.indexMap = retrieveIndexFromFiles(this.inputPathIndex);
		this.docLen = (HashMap<String, Integer>) (objectStreamInWordFreq.readObject());
		this.termLen = (HashMap<String, Integer>) (objectStreamIntermFreq.readObject());
	}

	@SuppressWarnings("unchecked")
	public LinkedList<Posting> mergePostingLists(LinkedList<Posting> list1, LinkedList<Posting> list2) {
		LinkedList<Posting> res = new LinkedList<Posting>();

		int i = 0;
		int j = 0;
		while (i<list1.size() && j<list2.size()){
			if (list1.get(i).compareTo(list2.get(j)) < 0) {
				res.add(list1.get(i));
				i++;
			}
			if (list1.get(i).compareTo(list2.get(j)) > 0) {
				res.add(list2.get(j));
				j++;
			}
			if (list1.get(i).compareTo(list2.get(j)) == 0) {
				String docID = list1.get(i).docID;
				Integer termFreq = list1.get(i).termFreq + list2.get(i).termFreq;
				Posting p = new Posting(docID, termFreq);
				res.add(p);
				i++;
				j++;
			}
		}
		if(i < list1.size()){
			while (i<list1.size()){
				res.add(list1.get(i));
				i++;
			}
		}
		if(j < list2.size()){
			while (j<list2.size()){
				res.add(list2.get(j));
				j++;
			}
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, LinkedList<Posting>> retrieveIndexFromFiles(String path) throws Exception{
		List<Path> files = Files.walk(Paths.get(path)).filter(Files::isRegularFile).filter(p -> p.toFile().getName().startsWith("index")).collect(Collectors.toList());

		ObjectInputStream[] arrObjectinputstream = new ObjectInputStream[files.size()];
		HashMap[] arrIndexMap = new HashMap[files.size()];
		HashMap<String, LinkedList<Posting>> index = new HashMap<String, LinkedList<Posting>>();

		// open streams and get indexes
		int counter = 0;
		for (Path file : files) {
			arrObjectinputstream[counter] = new ObjectInputStream(new FileInputStream(file.toFile()));
			arrIndexMap[counter] = (HashMap<String, LinkedList<Posting>>) (arrObjectinputstream[counter].readObject());

			// start with first
			for (String key : ((HashMap<String, LinkedList<Posting>>) arrIndexMap[counter]).keySet()) {
				if (!index.containsKey(key)) {
					index.put(key, ((HashMap<String, LinkedList<Posting>>) arrIndexMap[counter]).get(key));
				}
				else {
					LinkedList<Posting> list1 = index.get(key);
					LinkedList<Posting> list2 = ((HashMap<String, LinkedList<Posting>>) arrIndexMap[counter]).get(key);

					LinkedList<Posting> mergedList = mergePostingLists(list1, list2);
					index.put(key, mergedList);
				}
			}
			counter++;
		}

		// stream to whole index
		//FileOutputStream  fout = new FileOutputStream(outputPathIndex + "index.sr");
		//ObjectOutputStream oos = new ObjectOutputStream(fout);

		//oos.writeObject(index);
		return index;
	}

	public HashMap<String, Integer> getQueryTerms(String topicNr) throws IOException {

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

		Tokenizer tokenizer = new BasicTokenizerSP();
		List<PreprocessWord> preprocessWords = new LinkedList<>();
		preprocessWords.add(new StopWords());
		preprocessWords.add(new Stemmer());
		preprocessWords.add(new CaseFold());

		DocumentProcessing documentProcessing = new DocumentProcessing(tokenizer, preprocessWords);
		String[] queryTerms = documentProcessing.processLineToTerms(result);

		HashMap<String, Integer> queryWordCnt = new HashMap<String, Integer>();
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

		// query
		HashMap<String, Integer> queryTerms = new HashMap<String, Integer>();
		queryTerms.put("peace", 1);
		queryTerms.put("freedom", 1);
		/*getQueryTerms(topic);*/

		int collectionSize = docLen.size();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>();

		// for each query term
		for(String term : queryTerms.keySet()) {
			// get tfidf for the document
			LinkedList<Posting> postingsList = indexMap.get(term);
			if (postingsList != null && !postingsList.isEmpty()) {
				for (int j=0; j<postingsList.size(); j++) {
					int tfd = postingsList.get(j).termFreq;
					int df = postingsList.size();

					String docId = postingsList.get(j).docID;

					// get tfidf for the query
					double wtq = TfIdf.getWeight(queryTerms.get(term), df, collectionSize);
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
		}

		// divide by doc length
		for (String docId : scoreMap.keySet()) {
			double score =  scoreMap.get(docId) / (double) docLen.get(docId);
			scoreMap.put(docId, score);
		}

		printTopScore(scoreMap, topic);
	}

	public void cosineBM25(String topic, double k1, double k3, double b) throws IOException  {

		// calc avg len
		double sumLen = 0;
		for (String doc : docLen.keySet()) {
			sumLen = sumLen + docLen.get(doc);
		}
		double avgld = sumLen / (double) docLen.size();

		// query
		HashMap<String, Integer> queryTerms = getQueryTerms(topic);

		int collectionSize = docLen.size();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>();

		// for each query term
		for(String term : queryTerms.keySet()) {
			// get bm25 for the document
			LinkedList<Posting> postingsList = indexMap.get(term);
			if (postingsList != null && !postingsList.isEmpty()) {
				for (int j=0; j<postingsList.size(); j++) {
					int tfd = postingsList.get(j).termFreq;
					int df = postingsList.size();

					String docId = postingsList.get(j).docID;

					int tfq = queryTerms.get(term);
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

		printTopScore(scoreMap, topic);
	}

	public void cosineBM25VA(String topic, double k1, double k3) throws IOException  {

		// calc avg len
		double sumLen = 0;
		for (String doc : docLen.keySet()) {
			sumLen = sumLen + docLen.get(doc);
		}
		double avgld = sumLen / (double) docLen.size();

		double sumAvgtf = 0;
		for (String doc : termLen.keySet()) {
			double avgtf = ((double) docLen.get(doc)) / ((double) termLen.get(doc));
			sumAvgtf = sumAvgtf + avgtf;
		}
		double mavgtf = sumAvgtf / (double) docLen.size();

		// query
		HashMap<String, Integer> queryTerms = getQueryTerms(topic);

		// calculate score
		int collectionSize = docLen.size();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>();


		// for each query term
		for(String term : queryTerms.keySet()) {
			// get bm25va for the document
			LinkedList<Posting> postingsList = indexMap.get(term);
			if (postingsList != null && !postingsList.isEmpty()) {
				for (int j=0; j<postingsList.size(); j++) {
					int tfd = postingsList.get(j).termFreq;
					int df = postingsList.size();

					String docId = postingsList.get(j).docID;

					int tfq = queryTerms.get(term);
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

		printTopScore(scoreMap, topic);
	}

	public void printTopScore(HashMap<String, Double> scoreMap, String topic) {

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
		for (double docScore : treeMap.keySet()) {
			System.out.println(topic + " Q0 " + treeMap.get(docScore) + " " + rank + " " + docScore + " " + runname);
			rank++;
		}
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
}
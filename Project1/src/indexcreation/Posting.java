package indexcreation;

import java.io.Serializable;

public class Posting implements Serializable, Comparable<Posting> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String docID;
    public int termFreq;

    public Posting(String docID) {
        this.docID = docID;
        this.termFreq = 1;
    }

    public Posting(String docID, Integer termFreq) {
        this.docID = docID;
        this.termFreq = termFreq;
    }    
    
    @Override
    public int compareTo(Posting anotherPosting) {
        return anotherPosting.docID.compareTo(this.docID);
    }
}

package indexcreation;

public class Posting {

    public String docID;
    public int termFreq;

    public Posting(String docID) {
        this.docID = docID;
        this.termFreq = 1;
    }


}

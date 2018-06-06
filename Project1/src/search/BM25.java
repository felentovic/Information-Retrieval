package search;

public class BM25 {
	
	private double k1;
	private double k3;
	private double b;
	
	public BM25(double k1, double k3, double b) {
		
		this.k1 = k1;
		this.k3 = k3;
		this.b = b;
	}
	
	public double getWeight(double tfd, double tfq, double df, double ld, double avgld, double cSize) {		
		double Bd = (1 - b) + (b * (ld / avgld));
		return (tfq / (k3 + tfq)) * 
			(tfd / (k1*Bd + tfd)) * 
			 Math.log((cSize - df + 0.5) / (df - 0.5));
	}
}

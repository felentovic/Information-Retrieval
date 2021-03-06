package search;

public class BM25VA {
	
	private double k1;
	private double k3;
	
	public BM25VA(double k1, double k3) {
		
		this.k1 = k1;
		this.k3 = k3;
	}
	
	public double getWeight(double tfd, double tfq, double df, double ld, double td, double avgld, double mavgtf, double cSize) {		
		
		/*
		System.out.println("tfd = " + tfd);;
		System.out.println("tfq = " + tfq);;
		System.out.println("df = " + df);;
		System.out.println("ld = " + ld);;
		System.out.println("td = " + td);;
		System.out.println("avgld = " + avgld);;
		System.out.println("mavgtf = " + mavgtf);;
		System.out.println("cSize = " + cSize);;
		*/
		
		double Bva = (ld/(mavgtf*mavgtf*td)) + ((1 - (1/mavgtf)) * (ld/avgld));			
		return (tfq / (k3 + tfq)) * 
			(tfd / (k1*Bva + tfd)) * 
			 Math.log((cSize - df + 0.5) / (df - 0.5));	
	}
}

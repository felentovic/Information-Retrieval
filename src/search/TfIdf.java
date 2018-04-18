package search;

public class TfIdf {

		public static double getWeight(int tf, int df, int cSize) {
			return Math.log(1 + (double) tf) * Math.log((double)cSize / (double)df);
		}
	
}

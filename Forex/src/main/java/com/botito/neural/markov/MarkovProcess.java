package com.botito.neural.markov;

public class MarkovProcess {
	private double[] markovExmples;
	private double[][] markovMatrix;
	
	public MarkovProcess(double[][] examples,int column, int window) {
		markovMatrix = new double[examples[0].length][2];
		for (int i = window; i < examples[0].length; i++) {
			double[][] markovLine = new double[window][2];
			double example = -1;
			int down = 0;
			int up = 0;
			for (int j =  (i - window); j <= i; j++) {
				if (j > 0) {
					if (example >  examples[j][column]) {
						up++;
					} else {
						down++;
					}
				}
				if (j == (i - window)) {
					markovLine[j][0] = (up/(up+down));
					markovLine[j][1] = (down/(up+down));
				}
				example = examples[j][column];

			}
		}		
	}
	
	public void process(double[][] markovLine) {
	
	}
}

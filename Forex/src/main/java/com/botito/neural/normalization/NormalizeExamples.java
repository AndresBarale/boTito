package com.botito.neural.normalization;

import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

public class NormalizeExamples {
	
	private double[][] normalizeFields;
	private double[][] exampleNormalized;
	
	public NormalizeExamples(double[][] examples) {
		normalizeFields = new double[examples.length][examples[0].length];
		exampleNormalized = new double[examples.length][examples[0].length];
		normilizedFields(examples);
		normalize(examples);
	}
	
	public void normilizedFields(double[][] examples) {
		double min = 0;
		double max = 1;
		for (int i = 0; i < examples[0].length; i++) {
			double[] norm = new double[examples.length];
			for (int k = 0; k < examples.length; k++) {
				norm[k] = examples[k][i];
			}
			
			min = getMin(norm);
			max = getMax(norm);
			//normalizeFields[i] = new NormalizedField(NormalizationAction.Equilateral, 
			//		null,max,min,1,0);
			if(max - min > 0) {
				for (int k = 0; k < examples.length; k++) {
					normalizeFields[k][i] = (examples[k][i]-min)/(max-min); 
				}
			}		
			
		}
		
	}
	
	public void normalize(double[][] examples) {
		for (int i = 0; i < examples.length; i++) {
			for (int j = 0; j < examples[i].length; j++) {
				if (!Double.isNaN(examples[i][j]) && !Double.isNaN(normalizeFields[i][j])/* && !Double.isNaN(normalizeFields[j].normalize(examples[i][j]))*/) {
					//exampleNormalized[i][j] = normalizeFields[j].normalize(examples[i][j]);
					exampleNormalized[i][j] = normalizeFields[i][j];
				} else {
					exampleNormalized[i][j] = 0.0;
				}
			}
		}	
	}
	
	public double[][] getNormalized() {
		return exampleNormalized;
	}
	
	public double[][] getDeNormalized() {
		double[][] denormalized = new double[exampleNormalized.length][exampleNormalized[0].length];
		for (int i = 0; i < exampleNormalized.length; i++) {
			for (int j = 0; j < exampleNormalized[i].length; j++) {
				//denormalized[i][j] = normalizeFields[i].deNormalize(exampleNormalized[i][j]);
			}
		}	
		return denormalized;
	}
	
	public double getMin(double[] examples) {
		double ret = 10000000000.000;
		for (int i = 0; i < examples.length; i++) {
			ret = Math.min(examples[i], ret);
		}
		return ret;
	}

	public double getMax(double[] examples) {
		double ret = 0;
		for (int i = 0; i < examples.length; i++) {
			ret = Math.max(examples[i], ret);
		}
		return ret;
	}
}

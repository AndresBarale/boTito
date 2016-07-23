package com.botito.bo;

import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

public class NormalizeExamples {
	
	private NormalizedField[] normalizeFields;
	private double[][] exampleNormalized;
	
	public NormalizeExamples(double[][] examples) {
		normalizeFields = new NormalizedField[examples[0].length];
		exampleNormalized = new double[examples.length][examples[0].length]; 		
		double[] norm = normilizedFields(examples);
		normalize(examples);
	}
	
	public double[] normilizedFields(double[][] examples) {
		double[] norm = new double[examples[0].length];
		for (int i = 0; i < examples[0].length; i++) {
			for (int k = 0; k < examples.length; k++) {
				norm[i] = examples[i][k];
			}
			double min = getMin(norm);
			double max = getMax(norm);
			normalizeFields[i] = new NormalizedField(NormalizationAction.Equilateral, 
					null,max,min,1,0);
			norm = new double[examples[0].length];
			
		}
		return norm;
	}
	
	public void normalize(double[][] examples) {
		for (int i = 0; i < examples.length; i++) {
			for (int j = 0; j < examples[i].length; j++) {
				exampleNormalized[i][j] = normalizeFields[i].normalize(examples[i][j]);
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
				denormalized[i][j] = normalizeFields[i].deNormalize(exampleNormalized[i][j]);
			}
		}	
		return denormalized;
	}
	
	public double getMin(double[] examples) {
		double ret = 0;
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

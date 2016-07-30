package com.botito.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


public class ReadCSV {
	private String idealString = "direction0";
	private double[][] ideal = null;
	private String[] days = {};
	private final static Logger log = Logger.getLogger(ReadCSV.class);

	public double[][] readCSV(String pathCSV) {
	
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		List<Double> arrForex = new ArrayList<Double>();
		List<Double> arrForexIdeal = new ArrayList<Double>();
		List<String> daysLines = new ArrayList<String>();
		double[][] forexMatrix = null;
		try {
			br = new BufferedReader(new FileReader(pathCSV));
			int numLines = 0;
			int numValues = 0;
			int posIdeal = -1;
			
			if ((line = br.readLine()) != null) {
				String[] forexValues = line.split(cvsSplitBy);
				
				numValues = forexValues.length;
				for (int i = 1; i < forexValues.length; i++) {
					if (forexValues[i].equals(idealString)) {
						posIdeal = i;
						break;
					}
				}
			}
			while ((line = br.readLine()) != null) {
			    // use comma as separator	
				String[] forexValues = line.split(cvsSplitBy);
				daysLines.add(forexValues[0]);
				for (int i = 1; i < forexValues.length; i++) {
					if(i != posIdeal) {
						arrForex.add(Double.parseDouble(forexValues[i]));
					} else {
						arrForexIdeal.add(Double.parseDouble(forexValues[i]));
					}
				}
				numLines++;
			}
			forexMatrix = new double[numLines][numValues -2]; 
			Iterator<Double> iterDouble = arrForex.iterator();
			int i = 0;
			int numLinesFinal = 0;
			while (iterDouble.hasNext()) {
				if (i == numValues - 2) {
					i = 0;
					numLinesFinal++;
				}
				double value = iterDouble.next();
				forexMatrix[numLinesFinal][i] = value;
				i++;
			}
			int j = 0;
			days = new String[numLines];
			for(String day : daysLines) {
				days[j] = day;
				j++;
			}
			ideal = new double[numLines][1]; 
			Iterator<Double> iterDoubleIdeal = arrForexIdeal.iterator();
			int numLinesIdealFinal = 0;
			while (iterDoubleIdeal.hasNext()) {
				double value = iterDoubleIdeal.next();
				ideal[numLinesIdealFinal][0] = value;
				numLinesIdealFinal++;
			}

		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}           
		return forexMatrix;		
	}

	public double[][] getIdeal() {
		return ideal;
	}

	public void setIdeal(double[][] ideal) {
		this.ideal = ideal;
	}
	
	public String getIdealString() {
		return idealString;
	}

	public void setIdealString(String idealString) {
		this.idealString = idealString;
	}

	public String[] getDays() {
		return days;
	}

	public void setDays(String[] days) {
		this.days = days;
	}
	
	
}

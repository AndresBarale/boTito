package com.botito.run.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.log4j.Logger;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.manhattan.ManhattanPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.simple.EncogUtility;

import com.botito.csv.ReadCSV;
import com.botito.neural.activation.ActivationQuadraticSine;
import com.botito.neural.normalization.NormalizeExamples;
import com.botito.neural.propagation.ForexNeural;


public class ForexNeuralTest {
	
	private static int epochs = 5600;
	private static double errorTrain = 0.000001;
	private int hiddenNeurons = 70; // for stock market
	private  int hiddenNeurons2 = 0;
	private int lastTest = 15;
	private int lastTestLearn = 30;
	private static double learningRate = 0.003;
	private static double probe = 0.59;
	private static double probeTrain = 0.59;
	private static int tryModel = 10;
	private static String model = null;
	private double toleranceErrorBuy = 0.0;
	private double toleranceErrorSell = 1.00;
	private double toleranceErrorLearn = 0.17;
	private Propagation trainNet;
	private BasicNetwork networkLearn;
	//private int asserts = 20;
	private double assertsProb;
	private int assertsProbWrite;
	private int countWrite;
	private double openTotalWinWrite;
	private double loseTotalWrite;
	private final static Logger log = Logger.getLogger(ForexNeural.class);
	
	public void learn(String pathCSV, String file, int testDaysAgo) throws Exception {	
		int i = 0;
		int assertPrediction = 0;
		double e1 = 100;
		double openTotalWin = 0;
		double openTotalLose = 0;
		int mh4 = 0;
		int tryLearn = 0;
		Propagation train = null;
		
		do {
			
			int lastTestTrain = lastTest + lastTestLearn + testDaysAgo;
			log.debug("Learn!!! Dia: "+ lastTestTrain + " Archive: " + file + " Started.");
			long time = System.currentTimeMillis();
			ReadCSV readCSV = new ReadCSV();
			double[][] data = readCSV.readCSV(pathCSV + file);
			if (data == null || data.length == 0) {
				return;
			}
			
			//hiddenNeurons2 = Math.round(data[0].length/3) + 1;
			NormalizeExamples norm  = new NormalizeExamples(data);
			double[][] ideal = readCSV.getIdeal();
			double[][] dataNormalized = norm.getNormalized();
			double[][] dataTrain = new double[data.length - lastTestTrain][data[0].length]; 
			double[][] idealTrain = new double[data.length - lastTestTrain][1]; 
			double[][] dataTest = new double[lastTestTrain][data[0].length]; 
			double[][] idealTest = new double[lastTestTrain][1];
			double[][] dataTestOne = new double[1][data[0].length]; 
			double[][] idealTestOne = new double[1][1];
			
			double close = 0;
			double open = data[data.length - lastTestTrain][3];
			if	(lastTest > 1) {
				close = data[data.length - lastTestTrain + 1][8];
			}
			
			createArraysTester(
					data,
					dataNormalized,
					dataTrain,
					idealTrain,
					dataTest,
					ideal,
					idealTest, lastTestTrain);
			//idealTestMinusOne[0][0] = null;
			//idealTestMinusOne[0][0] = ideal4h[mh4];
			
			mh4++;
			// create a neural network, without using a factory
			//BasicNetwork network = new BasicNetwork();
			BasicNetwork network = new BasicNetwork();
			hiddenNeurons = Math.round(data[0].length/2) + 1;
			network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,data[0].length));
			network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,hiddenNeurons + hiddenNeurons2));
			//network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,hiddenNeurons2));
			network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,1));
			network.getStructure().finalizeStructure();
			network.reset();
			
			// create training data
			int epoch = 1;
	
			MLDataSet trainingSet = new BasicMLDataSet(dataTrain,idealTrain);
			train = new ResilientPropagation(network,trainingSet);
			if(model != null && model.equals("mhn")) {
				train = new ManhattanPropagation(network, trainingSet,learningRate);
			} 
			do {
				train.iteration();
				//log.info("Epoch #" + epoch + " Archive: " + file  +" Error:" + train.getError());
				epoch++;
			} while(train.getError() > errorTrain && epoch <= epochs);
			train.finishTraining();
			log.info("Learn!!! Dia: "+ lastTestTrain + 
					" Epoch #" + epoch + " Archive: " + file  +" Error:" + train.getError() + " Segs: " + (double)(System.currentTimeMillis() - time) / 1000);
			//Propagation train = new Backpropagation(network,trainingSet);
			// train the neural network
			do {
				//readCSV
		
				dataTestOne[0] = dataTest[dataTest.length - lastTestTrain];
				idealTestOne[0][0] = idealTest[idealTest.length - lastTestTrain][0];
				MLDataSet trainingSet2 = new BasicMLDataSet(dataTestOne,idealTestOne);
				String actual1 = "";
				int k = 0;
				for (final MLDataPair pair : trainingSet2) {
					if (k == 0) {	
						final MLData output = network.compute(pair.getInput());
						actual1 = "" + Math.round(Double.parseDouble(EncogUtility.formatNeuralData(output).replace(",", ".")));
					}
					k++; 
						
				}
				
				e1 = network.calculateError(trainingSet2);
				
				String idealTrainTest = "" + Math.round(idealTest[idealTest.length - lastTestTrain][0]);
				if (e1 < toleranceErrorLearn) {
					i++;
				
					 
					log.info("Learn!!! Dia: "+ lastTestTrain + " Archive: " + file  +" prediction: " + actual1 +  " actual: " + idealTrainTest);
					if (idealTrainTest.equals(actual1)) {
						assertPrediction++;
						openTotalWin += Math.abs(close - open); 
					} else {
						openTotalLose += Math.abs(close - open); 
					}
					
					log.info("Learn!!! Dia: "+ lastTestTrain + " Archive: " + file  + "  Network traiined to error: " + e1 );
			
					//EncogUtility.evaluate(network, trainingSet2);
					//Encog.getInstance().shutdown();
				} else {
					log.info("Learn!!! Dia: "+ lastTestTrain + " Archive: " + file  + " Try to predict: " + actual1 + " " + idealTrainTest + " discarded by error " + e1);
				}
				
				lastTestTrain--;
				//log.info(network.toString());
			} while(lastTestTrain > lastTest + testDaysAgo);
			if(i > 0 && ((double)assertPrediction/(double)i) > probeTrain) {
				log.info("********************************************************");
				log.info("Learn!!! Archive: " + file  + " prob: " + 
							(double)((double)assertPrediction/(double)i)*100  + 
							"% efec. win: " + (openTotalWin - openTotalLose) + 
							" Gross win: " + openTotalWin +
							" Gross lose: " + openTotalLose +
							" cant: " + assertPrediction + " " + i );
							
				log.info("********************************************************" + mh4);
				trainNet = train;
				networkLearn = network;
				return;
				
			} 
			tryLearn++;
		}while(tryLearn < tryModel);
	}

	
	public void think(String pathCSV, String file, boolean write, int testDaysAgo) throws Exception {
		int buyOrSell = -1;
		int i = 0;
		int assertPrediction = 0;
		double e1 = 100;
		double openTotalWin = 0;
		double openTotalLose = 0;
		int mh4 = 0;
		int last = lastTest + testDaysAgo;
		do {
			//readCSV
			log.error("Dia: "+ last + " Archive: " + file + " Started.");
			long time = System.currentTimeMillis();
			ReadCSV readCSV = new ReadCSV();
			double[][] data = readCSV.readCSV(pathCSV + file);
			if (data == null || data.length == 0) {
				return;
			}
			
			//hiddenNeurons2 = Math.round(data[0].length/3) + 1;
			NormalizeExamples norm  = new NormalizeExamples(data);
			double[][] ideal = readCSV.getIdeal();
			double[][] dataNormalized = norm.getNormalized();
			double[][] dataTrain = new double[data.length - last][data[0].length]; 
			double[][] idealTrain = new double[data.length - last][1]; 
			double[][] dataTest = new double[last][data[0].length]; 
			double[][] idealTest = new double[last][1];
			double[][] idealTestOne = new double[1][1];
			double[][] dataTestOne = new double[1][data[0].length]; 
			double close = 0;
			double open = data[data.length - last][3];
			if	(last > 1) {
				close = data[data.length - last + 1][8];
			}
			//double[][] dataDeNormalized = norm.getDeNormalized();
			createArraysTester(
					data,
					dataNormalized,
					dataTrain,
					idealTrain,
					dataTest,
					ideal,
					idealTest, last);
			//idealTestMinusOne[0][0] = null;
			//idealTestMinusOne[0][0] = ideal4h[mh4];
			
			
			if (trainNet != null) {
				Propagation train = trainNet;
				BasicNetwork network = networkLearn;
				
				log.info("Dia: "+ last + 
						 " Archive: " + file  +" Error:" + train.getError() + " Segs: " + (double)(System.currentTimeMillis() - time) / 1000);
				
				
				dataTestOne[0] = dataTest[dataTest.length - last];
				
				MLDataSet trainingSet2 = new BasicMLDataSet(dataTestOne,idealTestOne);
				String actual1 = "";
				int k = 0;
				for (final MLDataPair pair : trainingSet2) {
					if (k == 0) {	
						final MLData output = network.compute(pair.getInput());
						actual1 = "" + Math.round(Double.parseDouble(EncogUtility.formatNeuralData(output).replace(",", ".")));
					}
					k++; 
						
				}
				buyOrSell = Integer.parseInt(actual1);
				e1 = network.calculateError(trainingSet2);
				String idealTrainTest = "" + Math.round(idealTest[idealTest.length - last][0]);

				log.info("Dia: "+ last + " Archive: " + file  +" prediction: " + actual1 +  " actual: " + idealTrainTest);
				if (idealTrainTest.equals(actual1) && last > testDaysAgo) {
					assertPrediction++;
					openTotalWin += Math.abs(close - open); 
					
					i++;
				} else if (last > testDaysAgo){
					openTotalLose += Math.abs(close - open); 
					i++;
				}
				
				log.info("Dia: "+ last + " Archive: " + file  + "  Network traiined to error: " + e1 );
				if (buyOrSell != -1 && write && last == testDaysAgo) {
					if ((e1 < toleranceErrorBuy && buyOrSell == 1) ||	(e1 < toleranceErrorSell && buyOrSell == 0)) {
						WriteOrderTest writeOrder  = new WriteOrderTest();
						writeOrder.writeOrder(pathCSV, file, buyOrSell, readCSV.getDays()[readCSV.getDays().length - last] );
						if (idealTrainTest.equals(actual1)) {
							assertsProbWrite++;
							countWrite++;
						} else {
							countWrite++;
						}
						if (idealTrainTest.equals(actual1)) {							
							openTotalWinWrite += Math.abs(close - open); 	
						} else if (last == testDaysAgo){
							loseTotalWrite += Math.abs(close - open); 
						}
					} else {
						log.info("Order discarded by error: " + e1);
					}
				}			

			}
			last--;
			//log.info(network.toString());
		} while(last > (testDaysAgo -1));
		if(i > 0) {
			log.info("********************************************************");
			log.info("Archive: " + file  + " prob: " + 
						(double)((double)assertPrediction/(double)i)*100  + 
						"% efec. win: " + (openTotalWin - openTotalLose) + 
						" Gross win: " + openTotalWin +
						" Gross lose: " + openTotalLose +
						" cant: " + assertPrediction + " " + i );
						assertsProb = (double)((double)assertPrediction/(double)i);
			log.info("********************************************************" + mh4);
			
			
		}
		
			
	}	
	
	public void thinkSmart(String pathCSV, String file) throws Exception {
		for (int j = 25; j > 2; j--) {
			assertsProb = 0.0;
			for (int i = 0; i < 50; i++) {
				trainNet = null;
				learn(pathCSV,file, j);
				think(pathCSV,file, false, j);
				if (assertsProb > probe) {
					think(pathCSV,file, true, j);
					break;
				}
			}
			writeAsserts(pathCSV,file);
		}
		
	}
	
	private void writeAsserts(String pathCSV, String fileName) throws Exception {
		String content = "This is the conclusion of the file.";

		File file = new File(pathCSV + fileName + ".conclusion");

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		} else {
			file.delete();
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.newLine();
		bw.write("********************************************************");
		bw.newLine();
		bw.write("Archive: " + fileName  + " prob: ");
		bw.newLine();
		bw.write("percentage: "+	(double)((double)assertsProbWrite/(double)countWrite)*100); 
		bw.newLine();
		bw.write(			"% efec. win: " + (openTotalWinWrite - loseTotalWrite));
		bw.newLine();
		bw.write(			" Gross win: " + openTotalWinWrite);
		bw.newLine();
		bw.write(			" Gross lose: " + loseTotalWrite);
		bw.newLine();
		bw.write(			" cant: " + assertsProbWrite + " " + countWrite );
		bw.newLine();
		bw.write( "assertProb: " +  (double)((double)assertsProbWrite/(double)countWrite));
		bw.newLine();
		bw.write("********************************************************" );
		bw.newLine();
		bw.close();

		System.out.println("Done");
	}
	
	private void createArraysTester(
			double[][] data,
			double[][] dataNormalized,
			double[][] dataTrain,
			double[][] idealTrain,
			double[][] dataTest,
			double[][] ideal,
			double[][] idealTest, int last
			
		) {
		for (int i = 0; i < data.length; i++) {
			if (i < data.length - last) {
				for (int j = 1; j < data[0].length; j++) {
					dataTrain[i][j] = dataNormalized[i][j];
					 
				}
				idealTrain[i][0] = ideal[i][0];
			} else {
				int m = i - (data.length - last);
				for (int j = 0; j < data[0].length; j++) {
					dataTest[m][j] =  dataNormalized[i][j];
					 
				}
				idealTest[m][0] = ideal[i][0];
			}
		}
	}
	
	public int getEpochs() {
		return epochs;
	}

	public void setEpochs(int epochs) {
		ForexNeuralTest.epochs = epochs;
	}

	public double getErrorTrain() {
		return errorTrain;
	}

	public void setErrorTrain(double errorTrain) {
		ForexNeuralTest.errorTrain = errorTrain;
	}

	public int getHiddenNeurons() {
		return hiddenNeurons;
	}

	public void setHiddenNeurons(int hiddenNeurons) {
		this.hiddenNeurons = hiddenNeurons;
	}

	public int getLastTest() {
		return lastTest;
	}

	public void setLastTest(int lastTest) {
		this.lastTest = lastTest;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(double learningRate) {
		ForexNeuralTest.learningRate = learningRate;
	}

	public double getProbe() {
		return probe;
	}

	public void setProbe(double probe) {
		ForexNeuralTest.probe = probe;
	}

	public int getTryModel() {
		return tryModel;
	}

	public void setTryModel(int tryModel) {
		ForexNeuralTest.tryModel = tryModel;
	}

	public static String getModel() {
		return model;
	}

	public static void setModel(String model) {
		ForexNeuralTest.model = model;
	}


	public int getHiddenNeurons2() {
		return hiddenNeurons2;
	}


	public void setHiddenNeurons2(int hiddenNeurons2) {
		this.hiddenNeurons2 = hiddenNeurons2;
	}


	public double getToleranceErrorBuy() {
		return toleranceErrorBuy;
	}


	public void setToleranceErrorBuy(double toleranceErrorBuy) {
		this.toleranceErrorBuy = toleranceErrorBuy;
	}


	public double getToleranceErrorSell() {
		return toleranceErrorSell;
	}


	public void setToleranceErrorSell(double toleranceErrorSell) {
		this.toleranceErrorSell = toleranceErrorSell;
	}


	public double getToleranceErrorLearn() {
		return toleranceErrorLearn;
	}


	public void setToleranceErrorLearn(double toleranceErrorLearn) {
		this.toleranceErrorLearn = toleranceErrorLearn;
	}


	public int getLastTestLearn() {
		return lastTestLearn;
	}


	public void setLastTestLearn(int lastTestLearn) {
		this.lastTestLearn = lastTestLearn;
	}


	public static double getProbeTrain() {
		return probeTrain;
	}


	public void setProbeTrain(double probeTrain) {
		ForexNeuralTest.probeTrain = probeTrain;
	}

    
}

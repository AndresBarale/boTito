package com.botito.neural.propagation;

import java.io.File;

import org.apache.log4j.Logger;
import org.encog.engine.network.activation.ActivationSigmoid;
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
import com.botito.run.WriteOrder;


public class ForexNeural {
	
	private static int epochs = 5600;
	private static double errorTrain = 0.00001;
	private int hiddenNeurons = 70; // for stock market
	private static int hiddenNeurons2 = -40;
	private int lastTest = 30;
	private int lastTestLearn = 30;
	private static double learningRate = 0.003;
	private static double probe = 0.63;
	private static int tryModel = 10;
	private static String model = null;
	private double toleranceError = 0.22;
	private double toleranceErrorLearn = 0.03;
	private Propagation trainNet;
	private BasicNetwork networkLearn;
	private final static Logger log = Logger.getLogger(ForexNeural.class);
	
	public void learn(String pathCSV, String file) throws Exception {	
		int i = 0;
		int assertPrediction = 0;
		double e1 = 100;
		double openTotalWin = 0;
		double openTotalLose = 0;
		int mh4 = 0;
		int tryLearn = 0;
		Propagation train = null;
		
		do {
			
			int lastTestTrain = lastTest + lastTestLearn;
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
			//Propagation train = new Backpropagation(network,trainingSet);
			// train the neural network
			do {
				//readCSV
				
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
					if (idealTrainTest.equals(actual1) && lastTest > 1) {
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
			} while(lastTestTrain > lastTest);
			if(i > 0 && ((double)assertPrediction/(double)i) > probe) {
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

	
	public void think(String pathCSV, String file) throws Exception {
		int buyOrSell = -1;
		int i = 0;
		int assertPrediction = 0;
		double e1 = 100;
		double openTotalWin = 0;
		double openTotalLose = 0;
		int mh4 = 0;
		do {
			//readCSV
			log.error("Dia: "+ lastTest + " Archive: " + file + " Started.");
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
			double[][] dataTrain = new double[data.length - lastTest][data[0].length]; 
			double[][] idealTrain = new double[data.length - lastTest][1]; 
			double[][] dataTest = new double[lastTest][data[0].length]; 
			double[][] idealTest = new double[lastTest][1];
			double[][] idealTestOne = new double[1][1];
			double[][] dataTestOne = new double[1][data[0].length]; 
			double close = 0;
			double open = data[data.length - lastTest][3];
			if	(lastTest > 1) {
				close = data[data.length - lastTest + 1][8];
			}
			//double[][] dataDeNormalized = norm.getDeNormalized();
			createArraysTester(
					data,
					dataNormalized,
					dataTrain,
					idealTrain,
					dataTest,
					ideal,
					idealTest, lastTest);
			//idealTestMinusOne[0][0] = null;
			//idealTestMinusOne[0][0] = ideal4h[mh4];
			
			trainNet = null;
			learn(pathCSV,file);
			if (trainNet != null) {
				Propagation train = trainNet;
				BasicNetwork network = networkLearn;
				
				log.info("Dia: "+ lastTest + 
						 " Archive: " + file  +" Error:" + train.getError() + " Segs: " + (double)(System.currentTimeMillis() - time) / 1000);
				
				
				dataTestOne[0] = dataTest[dataTest.length - lastTest];
				
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
				String idealTrainTest = "" + Math.round(idealTest[idealTest.length - lastTest][0]);
				if (e1 > toleranceError) {
					i++;
				
					 
					log.info("Dia: "+ lastTest + " Archive: " + file  +" prediction: " + actual1 +  " actual: " + idealTrainTest);
					if (idealTrainTest.equals(actual1) && lastTest > 1) {
						assertPrediction++;
						openTotalWin += Math.abs(close - open); 
					} else {
						openTotalLose += Math.abs(close - open); 
					}
					
					log.info("Dia: "+ lastTest + " Archive: " + file  + "  Network traiined to error: " + e1 );
					if (buyOrSell != -1) {
						WriteOrder writeOrder  = new WriteOrder();
						writeOrder.writeOrder(pathCSV, file, buyOrSell, readCSV.getDays()[readCSV.getDays().length-lastTest]);
					}	
					//EncogUtility.evaluate(network, trainingSet2);
					//Encog.getInstance().shutdown();
				} else {
					log.info("Dia: "+ lastTest + " Archive: " + file  + " Try to predict: " + actual1 + " " + idealTrainTest + " discarded by error " + e1);
				}
			}
			lastTest--;
			//log.info(network.toString());
		} while(lastTest > 1);
		if(i > 0) {
			log.info("********************************************************");
			log.info("Archive: " + file  + " prob: " + 
						(double)((double)assertPrediction/(double)i)*100  + 
						"% efec. win: " + (openTotalWin - openTotalLose) + 
						" Gross win: " + openTotalWin +
						" Gross lose: " + openTotalLose +
						" cant: " + assertPrediction + " " + i );
			log.info("********************************************************" + mh4);
		}
		
		if (e1 > toleranceError && buyOrSell != -1) {
			ReadCSV readCSV = new ReadCSV();
			double[][] data = readCSV.readCSV(pathCSV + file);
			WriteOrder writeOrder  = new WriteOrder();
			writeOrder.writeOrder(pathCSV, file, buyOrSell, readCSV.getDays()[readCSV.getDays().length-1] );
		} else {
			File file1 = new File(pathCSV + file);

	         // File (or directory) with new name
	         File file2 = new File(pathCSV + file + ".txt");
	
	         if (file2.exists())
	            file2.delete();
	
	         // Rename file (or directory)
	         file1.renameTo(file2);
	         log.info("Archive: " + file  +" discarded by error " + e1);
				
		}
			
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
		ForexNeural.epochs = epochs;
	}

	public double getErrorTrain() {
		return errorTrain;
	}

	public void setErrorTrain(double errorTrain) {
		ForexNeural.errorTrain = errorTrain;
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
		ForexNeural.learningRate = learningRate;
	}

	public double getProbe() {
		return probe;
	}

	public void setProbe(double probe) {
		ForexNeural.probe = probe;
	}

	public int getTryModel() {
		return tryModel;
	}

	public void setTryModel(int tryModel) {
		ForexNeural.tryModel = tryModel;
	}

	public static String getModel() {
		return model;
	}

	public static void setModel(String model) {
		ForexNeural.model = model;
	}


	public double getToleranceError() {
		return toleranceError;
	}


	public void setToleranceError(double toleranceError) {
		this.toleranceError = toleranceError;
	}
	
	
	
	
	
	

}

package com.botito.neural.propagation;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;
import org.encog.ml.MLResettable;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.RequiredImprovementStrategy;
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


public class ForexNeuralTest {
	
	private static int epochs = 500;
	private static double errorTrain = 0.003;
	private static int hiddenNeurons = 150;
	private static int hiddenNeurons2 = 150;
	private static int lastTest = 31;
	private static double learningRate = 0.003;
	private static double probe = 0.63;
	private static int tryModel = 1;
	private static String model = null;
	
	public void think(String pathCSV, String file) {
		double prob1 = 0.0;
		int qModel = 0;
		int buyOrSell;
		//readCSV
		
		//do {
			ReadCSV readCSV = new ReadCSV();
			double[][] data = readCSV.readCSV(pathCSV + file);
			hiddenNeurons = Math.round(data[0].length/2) + 1;
			hiddenNeurons2 = Math.round(data[0].length/4) + 1;
			NormalizeExamples norm  = new NormalizeExamples(data);
			double[][] ideal = readCSV.getIdeal();
			double[][] dataNormalized = norm.getNormalized();
			double[][] dataTrain = new double[data.length - lastTest][data[0].length]; 
			double[][] idealTrain = new double[data.length - lastTest][1]; 
			double[][] dataTest = new double[lastTest][data[0].length]; 
			double[][] idealTest = new double[lastTest][1]; 
			createArrays(
					data,
					dataNormalized,
					dataTrain,
					idealTrain,
					dataTest,
					ideal,
					idealTest);
			
			// create a neural network, without using a factory
			//BasicNetwork network = new BasicNetwork();
			BasicNetwork network = new BasicNetwork();
			network.addLayer(new BasicLayer(null,true,data[0].length));
			network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,hiddenNeurons));
			network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,hiddenNeurons2));
			network.addLayer(new BasicLayer(new ActivationLinear(),true,1));
			network.getStructure().finalizeStructure();
			network.reset();
			
			// create training data
			int epoch = 1;
	
			MLDataSet trainingSet = new BasicMLDataSet(dataTrain,idealTrain);
			//Propagation train = new ResilientPropagation(network,trainingSet);
		
			// train the neural network
//			if(model != null && model.equals("mhn")) {
//				train = new ManhattanPropagation(network, trainingSet,learningRate);
//			} 
//			do {
//				train.iteration();
//				System.out.println("Epoch #" + epoch + " Archive: " + file + " Error:" + train.getError());
//				epoch++;
//			} while(train.getError() > errorTrain && epoch <= epochs);
//			train.finishTraining();
			
			/*
			SVM network2 = new SVM(data[0].length,true);
			final SVMTrain train = new SVMTrain(network2, trainingSet);

	 
			do {
				train.iteration();
				System.out.println("Epoch #" + epoch + " Error:" + train.getError());
				epoch++;
			} while(train.getError() > errorTrain && epoch <= 1);
			train.finishTraining();
	 		*/	
			
			this.xorGenetic(dataTrain,idealTrain);
			/*
			// second, create the data set		
			MLDataSet dataSet = new BasicMLDataSet(dataTrain,idealTrain);
			
			// third, create the trainer
			MLTrainFactory trainFactory = new MLTrainFactory();	
			MLTrain train = trainFactory.create(method,dataSet,trainerName,trainerArgs);				
			// reset if improve is less than 1% over 5 cycles
			if( method instanceof MLResettable && !(train instanceof ManhattanPropagation) ) {
				train.addStrategy(new RequiredImprovementStrategy(500));
			}

			// fourth, train and evaluate.
			EncogUtility.trainToError(train, 0.01);
			method = train.getMethod();
			EncogUtility.evaluate((MLRegression)method, dataSet);
			MLDataSet trainingSet2 = new BasicMLDataSet(dataTest,idealTest);
			double di = 0;
			double dj = 0;
			String actual1 = "";
			for (final MLDataPair pair : trainingSet2) {
				if(di < lastTest -1) {
					final MLData output = network.compute(pair.getInput());
					actual1 = "" + Math.round(Double.parseDouble(EncogUtility.formatNeuralData(output).replace(",", ".")));
					
					String idealTestString = "" + pair.getIdeal().getData(0) ;
					if (actual1.substring(0,1).equals(idealTestString.substring(0,1))) {
						dj++;
					}
				
					System.out.println(""
							+ "actual1=" + actual1.substring(0,1) + " ,ideal=" + idealTestString.substring(0,1));
					
					di++;
				} else {
					final MLData output = network.compute(pair.getInput());
					actual1 = "" + Math.round(Double.parseDouble(EncogUtility.formatNeuralData(output).replace(",", ".")));
				}	
			}
			
			prob1 = (double)dj/di;
			System.out.println(" prob: " + (dj/di)*100  + "% efec. cant: " + dj + " " + di);
			System.out.println("prediction: " + actual1);
			
			buyOrSell = Integer.parseInt(actual1);
			
			double e1 = network.calculateError(trainingSet2);
			System.out.println("Network traiined to error: " + e1 + "  prob: " + prob1);
	
			//EncogUtility.evaluate(network, trainingSet2);
			Encog.getInstance().shutdown();
			qModel++;
			//System.out.println(network.toString());
		} while(prob1 < probe && qModel <= tryModel);
		if (prob1 >= probe) {		
			WriteOrder writeOrder  =new WriteOrder();
			writeOrder.writeOrder(pathCSV, file, buyOrSell);
		}	
		*/
	}
	
	public void thinkWithoutTry(String pathCSV, String file) {
		double prob1 = 0.0;
		int qModel = 0;
		int buyOrSell;

		//readCSV
		ReadCSV readCSV = new ReadCSV();
		double[][] data = readCSV.readCSV(pathCSV + file);
		hiddenNeurons = Math.round(data[0].length/2) + 1;
		NormalizeExamples norm  = new NormalizeExamples(data);
		double[][] ideal = readCSV.getIdeal();
		double[][] dataNormalized = norm.getNormalized();
		double[][] dataTrain = new double[data.length - 1][data[0].length]; 
		double[][] idealTrain = new double[data.length - 1][1]; 
		double[][] dataTest = new double[data.length - 1][data[0].length]; 
		double[][] idealTest = new double[data.length - 1][1]; 
		for (int i = 0; i < data.length; i++) {
			if (i < data.length -1) {
				for (int j = 0; j < data[0].length; j++) {
					dataTrain[i][j] = dataNormalized[i][j];
					 
				}
				idealTrain[i][0] = ideal[i][0];
			} else {
				int m = i - (data.length - lastTest);
				for (int j = 0; j < data[0].length; j++) {
					dataTest[m][j] = dataNormalized[i][j];
					 
				}
				idealTest[m][0] = ideal[i][0];
				
			}
		}
		
		// create a neural network, without using a factory
		//BasicNetwork network = new BasicNetwork();
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,true,data[0].length));
		network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,hiddenNeurons));
		network.addLayer(new BasicLayer(new ActivationQuadraticSine(),true,1));
		network.getStructure().finalizeStructure();
		network.reset();
		
		// create training data
		int epoch = 1;

		MLDataSet trainingSet = new BasicMLDataSet(dataTrain,idealTrain);
		Propagation train = new ResilientPropagation(network,trainingSet);
		// train the neural network
		if(model != null && model.equals("mhn")) {
			train = new ManhattanPropagation(network, trainingSet,learningRate);
		} 
		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > errorTrain && epoch <= epochs);
		train.finishTraining();
		
		

		MLDataSet trainingSet2 = new BasicMLDataSet(dataTest,idealTest);
		double di = 0;
		double dj = 0;
		String actual1 = "";
		for (final MLDataPair pair : trainingSet2) {
		
			final MLData output = network.compute(pair.getInput());
			actual1 = "" + Math.round(Double.parseDouble(EncogUtility.formatNeuralData(output).replace(",", ".")));
				
		}
		
		//prob1 = (double)dj/di;
		//System.out.println(" prob: " + (dj/di)*100  + "% efec. cant: " + dj + " " + di);
		System.out.println("prediction: " + actual1);
		
		buyOrSell = Integer.parseInt(actual1);
		
		double e1 = network.calculateError(trainingSet2);
		System.out.println("Network traiined to error: " + e1 );

		//EncogUtility.evaluate(network, trainingSet2);
		Encog.getInstance().shutdown();
		qModel++;
		//System.out.println(network.toString());
		
		WriteOrder writeOrder  =new WriteOrder();
		writeOrder.writeOrder(pathCSV, file, buyOrSell);
			
	}	
	
	private void createArrays(
							double[][] data,
							double[][] dataNormalized,
							double[][] dataTrain,
							double[][] idealTrain,
							double[][] dataTest,
							double[][] ideal,
							double[][] idealTest
							
			) {
		for (int i = 0; i < data.length; i++) {
			if (i < data.length - lastTest) {
				for (int j = 0; j < data[0].length; j++) {
					dataTrain[i][j] = dataNormalized[i][j];
					 
				}
				idealTrain[i][0] = ideal[i][0];
			} else {
				int m = i - (data.length - lastTest);
				for (int j = 0; j < data[0].length; j++) {
					dataTest[m][j] = dataNormalized[i][j];
					 
				}
				idealTest[m][0] = ideal[i][0];
				
			}
		}
	}
	
	/**
	 * The input necessary for XOR.
	 */
	
	
	public static final String METHOD_FEEDFORWARD_A = "?:B->SIGMOID->4:B->SIGMOID->?";
	public static final String METHOD_FEEDFORWARD_RELU = "?:B->RELU->5:B->LINEAR->?";
	public static final String METHOD_BIASLESS_A = "?->SIGMOID->4->SIGMOID->?";
	public static final String METHOD_SVMC_A = "?->C->?";
	public static final String METHOD_SVMR_A = "?->R->?";
	public static final String METHOD_RBF_A = "?->gaussian(c=4)->?";
	public static final String METHOD_PNNC_A = "?->C(kernel=gaussian)->?";
	public static final String METHOD_PNNR_A = "?->R(kernel=gaussian)->?";
	
	/**
	 * Demonstrate a feedforward network with RPROP.
	 */
	public void xorRPROP(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_RPROP,
				"",1, input,  ouput);		
	}
	
	/**
	 * Demonstrate a feedforward network with RPROP & ReLu activation.
	 */
	public void xorRELU(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_RELU,
				MLTrainFactory.TYPE_RPROP,
				"",1, input,  ouput);		
	}
	

	
	/**
	 * Demonstrate a feedforward network with backpropagation.
	 */
	public void xorBackProp(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_BACKPROP,
				"",1, input,  ouput);		
	}
	

	/**
	 * Demonstrate a feedforward network with backpropagation.
	 */
	public void xorQProp(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_QPROP,
				"",1, input,  ouput);	
	}
	
	/**
	 * Demonstrate a SVM-classify.
	 */
	public void xorSVMClassify(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_SVM,
				this.METHOD_SVMC_A,
				MLTrainFactory.TYPE_SVM,
				"",1, input,  ouput);	
	}
	
	/**
	 * Demonstrate a SVM-regression.
	 */
	public void xorSVMRegression(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_SVM,
				this.METHOD_SVMR_A,
				MLTrainFactory.TYPE_SVM,
				"",1, input,  ouput);		
	}
	
	/**
	 * Demonstrate a SVM-regression search.
	 */
	public void xorSVMSearchRegression(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_SVM,
				this.METHOD_SVMR_A,
				MLTrainFactory.TYPE_SVM_SEARCH,
				"",1, input,  ouput);		
	}
	
	/**
	 * Demonstrate a XOR annealing.
	 */
	public void xorAnneal(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_ANNEAL,
				"",1, input,  ouput);		
	}
	
	/**
	 * Demonstrate a XOR genetic.
	 */
	public void xorGenetic(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_GENETIC,
				"",1, input,  ouput);		
	}
	
	/**
	 * Demonstrate a XOR LMA.
	 */
	public void xorLMA(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_LMA,
				"",1, input,  ouput);		
	}
	
	/**
	 * Demonstrate a XOR LMA.
	 */
	public void xorNM(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_NELDER_MEAD,
				"",1, input,  ouput);	
	}
	
	/**
	 * Demonstrate a XOR LMA.
	 */
	public void xorManhattan(double[][] input, double[][] ouput) {
		process( 
				MLMethodFactory.TYPE_FEEDFORWARD,
				this.METHOD_FEEDFORWARD_A,
				MLTrainFactory.TYPE_MANHATTAN,
				"lr=0.0001",1, input,  ouput);		
	}
	

	

	

	
	public void process(String methodName, String methodArchitecture,String trainerName, String trainerArgs,int outputNeurons, double[][] input, double[][] ouput) {
		
		// first, create the machine learning method
		MLMethodFactory methodFactory = new MLMethodFactory();		
		MLMethod method = methodFactory.create(methodName, methodArchitecture, input[0].length, outputNeurons);
		
		// second, create the data set		
		MLDataSet dataSet = new BasicMLDataSet(input, ouput);
		
		// third, create the trainer
		MLTrainFactory trainFactory = new MLTrainFactory();	
		MLTrain train = trainFactory.create(method,dataSet,trainerName,trainerArgs);				
		// reset if improve is less than 1% over 5 cycles
		if( method instanceof MLResettable && !(train instanceof ManhattanPropagation) ) {
			train.addStrategy(new RequiredImprovementStrategy(500));
		}

		// fourth, train and evaluate.
		EncogUtility.trainToError(train, 0.01);
		method = train.getMethod();
		EncogUtility.evaluate((MLRegression)method, dataSet);
		
		// finally, write out what we did
		System.out.println("Machine Learning Type: " + methodName);
		System.out.println("Machine Learning Architecture: " + methodArchitecture);

		System.out.println("Training Method: " + trainerName);
		System.out.println("Training Args: " + trainerArgs);
		
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
		ForexNeuralTest.hiddenNeurons = hiddenNeurons;
	}

	public int getLastTest() {
		return lastTest;
	}

	public void setLastTest(int lastTest) {
		ForexNeuralTest.lastTest = lastTest;
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
	
	
	

}

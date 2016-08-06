package com.botito.run.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.encog.Encog;

import com.botito.neural.propagation.ForexNeural;

public class RunBotito  implements Runnable  {
	
	private String pathCSV;
	private static List<String> files = new ArrayList<String>();
	private final static Logger log = Logger.getLogger(ForexNeural.class);
	private static RunBotito instance =  null;
	
	public static RunBotito getInstance() {
		if (instance == null) {
			instance = new RunBotito();
		}
		return instance;
	}

	public String getPathCSV() {
		return pathCSV;
	}

	public void setPathCSV(String pathCSV) {
		this.pathCSV = pathCSV;
	}
	
	public void readFiles() {
		File dir = new File(pathCSV);
		String[] archives = dir.list();
		log.info("Date: " + new Date(System.currentTimeMillis()).toString() +  " Reading files...");
		if (archives == null && !dir.exists() && !dir.isDirectory()) {
		    System.out.println("is not a directory");
		} else { 
			for (int i = 0; i < archives.length; i++) {
				if (archives[i].endsWith(".csv") && !isLockArchve(archives[i],archives)) {
					if (!files.contains(archives[i])) {
						files.add(archives[i]);	  
						run();
					}
				}
			 }
		    
		}
	}

	@Override
	public void run() {
		String file = files.get(files.size()-1);
		if (!files.contains(file)) {
			return;
		}
		log.info("Processing file: " + file);
		File file3 = new File(this.pathCSV + file);
		if (!file3.exists()) {
			log.info("File not exist: " + file);
			files.remove(file);
			return;
		}
		
		ForexNeuralTest rprop = new ForexNeuralTest();
		try {
			setProperties(this.pathCSV, file ,rprop);
			log.info("Thinking file: " + file);
			rprop.thinkSmart(this.pathCSV, file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File file1 = new File(this.pathCSV + file);
		
        // File (or directory) with new name
        File file2 = new File(this.pathCSV + file + ".txt");

        if (file2.exists())
           file2.delete();

        // Rename file (or directory)
        file1.renameTo(file2);
		
		if (!files.remove(file)) {
			log.info("Cant remove: " + file);
		}
		if (existFiles()) {
			Encog.getInstance().shutdown();
			files = new ArrayList<String>();
			log.info("+++++++++++++++++++++++++++++++++++");
			log.info("Shutdown Encog.");
			log.info("+++++++++++++++++++++++++++++++++++");
		}
	}
	
	private boolean existFiles() {
		File dir = new File(pathCSV);
		String[] archives = dir.list();
		boolean exists = true;
		if (archives == null && !dir.exists() && !dir.isDirectory()) {
		    System.out.println("is not a directory");
		} else { 
			for (int i = 0; i < archives.length; i++) {
				if (archives[i].endsWith(".csv")) {
					exists = false;
				}
			 }
		}
		return exists;
	}
	
	public boolean isLockArchve(String file, String[] archives) {
		boolean ret = false;
		for (int j = 0; j < archives.length; j++) {
			if (archives[j].endsWith(".lock")) {
				return true;
			}
		}
		return ret;
	}
	

	private void setProperties(String path, String file, ForexNeuralTest rprop) {
		try {
		   
			String filePropeties = FilenameUtils.removeExtension(file);
			filePropeties = filePropeties.split("-")[0] + "-" + filePropeties.split("-")[1] + ".properties" ;
		    Properties propiedades = new Properties();
		    
		    propiedades
		     .load(new FileInputStream(path + filePropeties));
		 
		    
		    String hiddenNeurons2 = propiedades.getProperty("hiddenNeurons2");
		    String toleranceErrorBuy = propiedades.getProperty("toleranceErrorBuy");
		    String toleranceErrorLearn = propiedades.getProperty("toleranceErrorLearn");
		    String toleranceErrorSell = propiedades.getProperty("toleranceErrorSell");
		    String lastTest = propiedades.getProperty("lastTest");
		    String lastTestLearn = propiedades.getProperty("lastTestLearn");
		    String probe = propiedades.getProperty("probe");
		    String probeTrain =  propiedades.getProperty("probeTrain");
		    String hiddens = propiedades.getProperty("hiddens");	
		    List<Integer> hiddenLayerNeurons = new ArrayList<Integer>();
		    for (int i = 0;i < hiddens.split(",").length; i++) {
		    	hiddenLayerNeurons.add(Integer.parseInt(hiddens.split(",")[i]));
		    }
		    
		    rprop.setHiddenNeurons2(Integer.parseInt(hiddenNeurons2));
		    rprop.setLastTest(Integer.parseInt(lastTest));
		    rprop.setLastTestLearn(Integer.parseInt(lastTestLearn));
		    rprop.setToleranceErrorBuy(Double.parseDouble(toleranceErrorBuy));
		    rprop.setToleranceErrorLearn(Double.parseDouble(toleranceErrorLearn));
		    rprop.setToleranceErrorSell(Double.parseDouble(toleranceErrorSell));
		    rprop.setProbe(Double.parseDouble(probe));
		    rprop.setProbeTrain(Double.parseDouble(probeTrain));
		    rprop.setLayerNeurons(hiddenLayerNeurons);
		    
	    
	   } catch (FileNotFoundException e) {
		   log.error("File propieties not exist.");
	   } catch (IOException e) {
		   log.error("File propieties can't read.");
	   }
	}
	

		

}
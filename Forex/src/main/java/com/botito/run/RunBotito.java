package com.botito.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.encog.Encog;

import com.botito.neural.propagation.ForexNeural;

public class RunBotito implements Runnable {
	
	private String pathCSV;
	private static List<String> files = new ArrayList<String>();
	private final static Logger log = Logger.getLogger(RunBotito.class);
	
	private boolean isReadingFiles = false;
	
	private static RunBotito instance = null;
	
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
		if (isReadingFiles) {
			return;
		}
		isReadingFiles = true;
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
		
		ForexNeural rprop = new ForexNeural();
		setProperties(this.pathCSV, file, rprop);
		try {
			rprop.thinkSmart(this.pathCSV, file);
		} catch (Exception e) {
			log.error(e);
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			log.error(e);
		}
				
		File file1 = new File(this.pathCSV + file);
		
        // File (or directory) with new name
        File file2 = new File(this.pathCSV + file + ".txt");

        if (file2.exists())
           file2.delete();

        // Rename file (or directory)
        file1.renameTo(file2);
        
        files.remove(file);
	    if(files.size() == 0) {    
	        Encog.getInstance().shutdown();
	        isReadingFiles = false;
		}
	}
	
	public boolean isLockArchve(String file, String[] archives) {
		boolean ret = false;
		for (int j = 0; j < archives.length; j++) {
			if (archives[j].endsWith(".lock") && archives[j].startsWith(file.split(".")[0])) {
				return true;
			}
		}
		return ret;
	}
	

	private void setProperties(String path, String file, ForexNeural rprop) {
		try {
		   
			String filePropeties = FilenameUtils.removeExtension(file) + ".properties";
		    Properties propiedades = new Properties();
		    
		    propiedades
		     .load(new FileInputStream(path + filePropeties));
		 
		    
		    String hiddenNeurons2 = propiedades.getProperty("hiddenNeurons2");
		    String toleranceErrorBuy = propiedades.getProperty("toleranceErrorBuy");
		    String toleranceErrorLearn = propiedades.getProperty("toleranceErrorLearn");
		    String toleranceErrorSell = propiedades.getProperty("toleranceErrorSell");
		    String lastTest = propiedades.getProperty("lastTest");

		    rprop.setHiddenNeurons2(Integer.parseInt(hiddenNeurons2));
		    rprop.setLastTest(Integer.parseInt(lastTest));
		    rprop.setToleranceErrorBuy(Double.parseDouble(toleranceErrorBuy));
		    rprop.setToleranceErrorLearn(Double.parseDouble(toleranceErrorLearn));
		    rprop.setToleranceErrorSell(Double.parseDouble(toleranceErrorSell));
	    
	   } catch (FileNotFoundException e) {
		  log.error("File propieties not exist.");
	   } catch (IOException e) {
		   log.error("File propieties can't read.");
	   }
	}

}

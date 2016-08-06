package com.botito.run.test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class WriteOrderTest {
	
	private final static Logger log = Logger.getLogger(WriteOrderTest.class);
	
	public void writeOrder(String path, String file, int buyOrSell, String line) {
		File fileExist = null;
		File fileExistLock = null;
		FileWriter order = null;
		FileWriter orderLock = null;
		PrintWriter pw = null;

		try {
			line = FilenameUtils.removeExtension(file).split("-")[0] + "-" + FilenameUtils.removeExtension(file).split("-")[1] + "-" + line;  
			fileExistLock = new File(".lock");
			if (!fileExistLock.exists()) {
				fileExistLock.delete();
			} else {
				orderLock = new FileWriter(path + line  + ".order.lock");
				
	            pw = new PrintWriter(orderLock);
	            if (buyOrSell == 1) {
	            	pw.println("Buy");
	            } else {
	            	pw.println("Sell");
	            }
			}
			fileExist = new File(path + line + ".order");
			if (fileExist.exists()) {
				fileExist.delete();
			}
			order = new FileWriter(path + line  + ".order");
			
            pw = new PrintWriter(order);
            if (buyOrSell == 1) {
            	pw.println("Buy");
            } else {
            	pw.println("Sell");
            }

        } catch (Exception e) {
        	log.error(e);
        } finally {
	        try {
				if (null != order) {
					order.close();
				}   
				if (null != orderLock) {
					orderLock.close();
					File file1 = new File(path + FilenameUtils.removeExtension(file) + ".order.lock");
					file1.delete();
				}
	        } catch (Exception e2) {
	        	log.error(e2);
			}
		}
	}
}

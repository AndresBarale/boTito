package com.botito.run;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

public class WriteOrder {
	
	public void writeOrder(String path, String file, int buyOrSell) {
		File fileExist = null;
		File fileExistLock = null;
		FileWriter order = null;
		FileWriter orderLock = null;
		PrintWriter pw = null;
		try {
			fileExistLock = new File(path + FilenameUtils.removeExtension(file) + ".order.lock");
			if (!fileExistLock.exists()) {
				fileExistLock.delete();
			} else {
				orderLock = new FileWriter(path + FilenameUtils.removeExtension(file) + ".order.lock");
				
	            pw = new PrintWriter(orderLock);
	            if (buyOrSell == 1) {
	            	pw.println("Buy");
	            } else {
	            	pw.println("Sell");
	            }
			}
			fileExist = new File(path + FilenameUtils.removeExtension(file) + ".order");
			if (fileExist.exists()) {
				fileExist.delete();
			}
			order = new FileWriter(path + FilenameUtils.removeExtension(file) + ".order");
			
            pw = new PrintWriter(order);
            if (buyOrSell == 1) {
            	pw.println("Buy");
            } else {
            	pw.println("Sell");
            }
            File file1 = new File(path + file);

	         // File (or directory) with new name
	         File file2 = new File(path + file + ".txt");
	
	         if (file2.exists())
	            file2.delete();
	
	         // Rename file (or directory)
	         file1.renameTo(file2);
        } catch (Exception e) {
            e.printStackTrace();
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
	        	e2.printStackTrace();
			}
		}
	}
}

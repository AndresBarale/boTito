package com.botito.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.botito.neural.propagation.ForexNeural;

public class RunBotito implements Runnable {
	
	private String pathCSV;
	private static List<String> files = new ArrayList<String>();

	public String getPathCSV() {
		return pathCSV;
	}

	public void setPathCSV(String pathCSV) {
		this.pathCSV = pathCSV;
	}
	
	public void readFiles() {
		File dir = new File(pathCSV);
		String[] archives = dir.list();
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
		try {
			//rprop.thinkWithoutTry(this.pathCSV, file);
			//rprop.think(this.pathCSV, file);
			rprop.thinkWithoutTesterTry(this.pathCSV, file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		files.remove(file);
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

}

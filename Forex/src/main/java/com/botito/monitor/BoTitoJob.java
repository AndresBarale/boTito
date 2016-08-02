package com.botito.monitor;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.botito.run.RunBotito;

public class BoTitoJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

        // do something
    	RunBotito runBoTito = RunBotito.getInstance();
    	runBoTito.setPathCSV(BoTitoPath.getPath());
    	runBoTito.readFiles();


	}

}

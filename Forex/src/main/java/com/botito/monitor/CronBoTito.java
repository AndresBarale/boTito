package com.botito.monitor;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class CronBoTito {
	 public static void main( String[] args ) throws Exception
	    {
		 	BoTitoPath.setPath(args[0]);
	    	//Quartz 1.6.3
	    	//JobDetail job = new JobDetail();
	    	//job.setName("dummyJobName");
	    	//job.setJobClass(HelloJob.class);    	
	    	JobDetail job = JobBuilder.newJob(BoTitoJob.class)
			.withIdentity("dummyJobName", "group1").build();

		//Quartz 1.6.3
	    	//CronTrigger trigger = new CronTrigger();
	    	//trigger.setName("dummyTriggerName");
	    	//trigger.setCronExpression("0/5 * * * * ?");
	    	
	    	Trigger trigger = TriggerBuilder
			.newTrigger()
			.withIdentity("dummyTriggerName", "group1")
			.withSchedule(
				CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
			.build();
	    	
	    	//schedule it
	    	Scheduler scheduler = new StdSchedulerFactory().getScheduler();
	    	scheduler.start();
	    	scheduler.scheduleJob(job, trigger);
	    
	    }
}

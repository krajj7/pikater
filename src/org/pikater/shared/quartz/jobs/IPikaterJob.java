package org.pikater.shared.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.Trigger;

public interface IPikaterJob extends Job
{
	void buildJob(JobBuilder builder);
	Trigger getJobTrigger();
}
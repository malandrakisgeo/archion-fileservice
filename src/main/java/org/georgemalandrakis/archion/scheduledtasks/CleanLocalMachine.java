package org.georgemalandrakis.archion.scheduledtasks;

import io.dropwizard.jobs.Job;
import io.dropwizard.jobs.annotations.DelayStart;
import io.dropwizard.jobs.annotations.Every;
import org.georgemalandrakis.archion.service.ScheduledTaskService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/*
    Removes from the local machine files not accessed
 */
@DelayStart("10s")
@Every("24h")
public class CleanLocalMachine extends Job {
    ScheduledTaskService scheduledTaskService;

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        scheduledTaskService.cleanLocalMachine(); //every 24h

    }

    public void setNecessaryClasses(ScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }

}

package org.georgemalandrakis.archion.scheduledtasks;

import io.dropwizard.jobs.Job;
import io.dropwizard.jobs.annotations.DelayStart;
import io.dropwizard.jobs.annotations.Every;
import org.georgemalandrakis.archion.service.ScheduledTaskService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DelayStart("15s")
@Every("24h")
public class DeleteDuplicates extends Job {
    ScheduledTaskService scheduledTaskService;

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        scheduledTaskService.removeDuplicates(); //every 24h
    }

    public void setNecessaryClasses(ScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }
}

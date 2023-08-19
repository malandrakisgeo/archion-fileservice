package org.georgemalandrakis.archion.scheduledtasks;

import io.dropwizard.jobs.Job;
import io.dropwizard.jobs.annotations.DelayStart;
import io.dropwizard.jobs.annotations.Every;
import org.georgemalandrakis.archion.core.ArchionConstants;
import org.georgemalandrakis.archion.service.ScheduledTaskService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DelayStart("20s")
@Every("24h")
public class DeleteOldFiles extends Job {

    ScheduledTaskService scheduledTaskService;

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        scheduledTaskService.runDelete(ArchionConstants.FILES_DEFAULT_FILETYPE); //every 24h

    }

    public void setNecessaryClasses(ScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }


}

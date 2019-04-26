package org.fogbowcloud.app.jes.arrebol;

import java.util.Properties;

import javax.print.attribute.standard.JobState;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.JobExecutionSystem;

public class ArrebolJobExecutionSystem implements JobExecutionSystem {

    private static final Logger LOGGER = Logger.getLogger(ArrebolJobExecutionSystem.class);

    private final ArrebolRequestsHelper requestsHelper;

    public ArrebolJobExecutionSystem(Properties properties) {
        this.requestsHelper = new ArrebolRequestsHelper(properties);
    }

    @Override
    public String execute(JDFJob job) {
        LOGGER.info("Execution for the Job with id :[" + job.getId() + "] was started");

        return this.requestsHelper.submitJobToExecution(job);
    }

    @Override
    public JDFJob getJob(String jobId) {
        return requestsHelper.getJob(jobId);
    }

    @Override
    public JobState jobState(String executionId) {
        return null;
    }


    @Override
    public void stop(String executionId) {

    }
}

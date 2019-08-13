package org.fogbowcloud.app.jes.arrebol;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.jes.arrebol.models.JobExecArrebol;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;
import org.fogbowcloud.app.jes.exceptions.JobSubmissionException;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Implementation of the Arrebol Job Execution Service API communication.
 */
public class ArrebolJobExecutionService implements JobExecutionService {

    private static final Logger logger = Logger.getLogger(ArrebolJobExecutionService.class);

    private final ArrebolRequestsHelper requestsHelper;

    public ArrebolJobExecutionService(Properties properties) {
        this.requestsHelper = new ArrebolRequestsHelper(properties);
    }

    @Override
    public String submit(Job job)
            throws UnsupportedEncodingException, JobSubmissionException, ArrebolConnectException {

        return this.requestsHelper.submitToExecution(job);
    }

    @Override
    public JobExecArrebol status(String executionId) {
        logger.info("Getting the status of execution with id [" + executionId + "]");

        return this.requestsHelper.getExecutionStatus(executionId);
    }
}

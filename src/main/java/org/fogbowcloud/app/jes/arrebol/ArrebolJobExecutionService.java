package org.fogbowcloud.app.jes.arrebol;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JobState;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;
import org.fogbowcloud.app.jes.exceptions.SubmitJobException;

public class ArrebolJobExecutionService implements JobExecutionService {

    private static final Logger logger = Logger.getLogger(ArrebolJobExecutionService.class);

    private final ArrebolRequestsHelper requestsHelper;

    public ArrebolJobExecutionService(Properties properties) {
        this.requestsHelper = new ArrebolRequestsHelper(properties);
    }

    @Override
    public String submit(JDFJob job)
            throws UnsupportedEncodingException, SubmitJobException, ArrebolConnectException {
        logger.info("Job with id :[" + job.getId() + "] was submitted for execution.");

        return this.requestsHelper.submitToExecution(job);
    }

    @Override
    public JobState status(String executionId) {
        return null;
    }
}

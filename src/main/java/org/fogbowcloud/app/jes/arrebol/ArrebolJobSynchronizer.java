package org.fogbowcloud.app.jes.arrebol;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.dto.JobDTO;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.exceptions.GetJobException;

import java.util.Properties;

public class ArrebolJobSynchronizer implements JobSynchronizer {

	private final Logger LOGGER = Logger.getLogger(ArrebolJobSynchronizer.class);
	
    private final ArrebolRequestsHelper requestsHelper;

    public ArrebolJobSynchronizer(Properties properties) {
        this.requestsHelper = new ArrebolRequestsHelper(properties);
    }

    @Override
    public JDFJob synchronizeJob(JDFJob job) {
    	JDFJob newJob;
    	try {
    		newJob = job;
			JobDTO arrebolJob = this.requestsHelper.getJob(job.getJobIdArrebol());
		} catch (GetJobException e) {
			LOGGER.error(e.getMessage());
			newJob = job;
		}
        return newJob;
    }
}

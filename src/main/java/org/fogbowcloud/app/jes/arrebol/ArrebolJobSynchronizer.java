package org.fogbowcloud.app.jes.arrebol;

import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

import java.util.Properties;

public class ArrebolJobSynchronizer implements JobSynchronizer {

    private final ArrebolRequestsHelper requestsHelper;

    public ArrebolJobSynchronizer(Properties properties) {
        this.requestsHelper = new ArrebolRequestsHelper(properties);
    }

    @Override
    public JDFJob synchronizeJob(JDFJob job) {
        return job;
    }
}

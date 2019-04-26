package org.fogbowcloud.app.jes.arrebol;

import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

public class ArrebolJobSynchronizer implements JobSynchronizer {

    private final ArrebolRequestsHelper requestsHelper;

    @Override
    public JDFJob synchronizeJob(JDFJob job) {
        return job;
    }
}

package org.fogbowcloud.app.core.monitor;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jes.arrebol.JobSynchronizer;

public class JobStateMonitor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(JobStateMonitor.class);
    private JobDataStore jobDataStore;
    private JobSynchronizer jobSynchronizer;

    public JobStateMonitor(JobDataStore jobDataStore, JobSynchronizer jobSynchronizer) {
        this.jobDataStore = jobDataStore;
        this.jobSynchronizer = jobSynchronizer;
    }

    @Override
    public void run() {
        LOGGER.info("Running job state monitor.");
        List<JDFJob> jobs = jobDataStore.getAll()
            .stream()
            .filter(j -> !(j.getState().equals(JDFJobState.FINISHED) || j.getState().equals(JDFJobState.FAILED)))
            .collect(Collectors.toList());
        for (JDFJob job : jobs) {
            JDFJob jobUpdated = jobSynchronizer.synchronizeJob(job);
            LOGGER.info("Response from datastore update: " + jobDataStore.update(jobUpdated));
        }
    }
}

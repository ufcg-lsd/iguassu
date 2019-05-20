package org.fogbowcloud.app.core.monitor;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.arrebol.JobSynchronizer;
import java.util.ArrayList;

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
        ArrayList<JDFJob> jobs = (ArrayList<JDFJob>) jobDataStore.getAll();
        for (JDFJob job : jobs) {
            JDFJob jobUpdated = jobSynchronizer.synchronizeJob(job);
            LOGGER.info("Response from datastore update: " + jobDataStore.update(jobUpdated));
        }
    }
}

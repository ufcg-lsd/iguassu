package org.fogbowcloud.app.core.monitor;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.IguassuApplication;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

import java.util.ArrayList;

public class JobStateMonitor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(JobStateMonitor.class);
    private JobDataStore jobDataStore;

    public JobStateMonitor(JobDataStore jobDataStore) {
        this.jobDataStore = jobDataStore;
    }

    @Override
    public void run() {
        LOGGER.info("Running job state monitor.");
        ArrayList<JDFJob> jobs = (ArrayList<JDFJob>) jobDataStore.getAll();
        for(JDFJob job : jobs) {
            JDFJob jobUpdated = IguassuApplication.jobSynchronizer.synchronizeJob(job);
            LOGGER.info("Response from datastore update: " + jobDataStore.update(jobUpdated));
        }
    }
}

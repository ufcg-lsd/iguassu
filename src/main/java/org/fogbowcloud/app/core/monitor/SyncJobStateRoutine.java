package org.fogbowcloud.app.core.monitor;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JobState;
import org.fogbowcloud.app.jes.arrebol.Synchronizer;

import java.util.List;
import java.util.stream.Collectors;

public class SyncJobStateRoutine implements Runnable {

    private static final Logger logger = Logger.getLogger(SyncJobStateRoutine.class);
    private JobDataStore jobDataStore;
    private Synchronizer synchronizer;

    SyncJobStateRoutine(JobDataStore jobDataStore, Synchronizer synchronizer) {
        this.jobDataStore = jobDataStore;
        this.synchronizer = synchronizer;
    }

    @Override
    public void run() {
        List<JDFJob> jobs =
                jobDataStore.getAll().stream()
                        .filter(
                                j ->
                                        !(j.getState().equals(JobState.FINISHED)
                                                || j.getState().equals(JobState.FAILED)))
                        .collect(Collectors.toList());
        for (JDFJob job : jobs) {
            JDFJob jobUpdated = synchronizer.sync(job);
            boolean updateResult = jobDataStore.update(jobUpdated);

            if (updateResult) {
                logger.debug(
                        "Job ["
                                + job.getId()
                                + "] was successfully synchronized with its execution ["
                                + job.getExecutionId()
                                + "].");
            } else {
                logger.error("Job [" + job.getId() + "] was not synchronized with its execution.");
            }
        }
    }
}

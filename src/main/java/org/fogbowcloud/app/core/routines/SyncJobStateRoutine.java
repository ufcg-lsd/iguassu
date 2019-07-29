package org.fogbowcloud.app.core.routines;

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
    private Synchronizer<JDFJob> synchronizer;

    SyncJobStateRoutine(JobDataStore jobDataStore, Synchronizer<JDFJob> synchronizer) {
        this.jobDataStore = jobDataStore;
        this.synchronizer = synchronizer;
    }

    @Override
    public void run() {
        logger.debug(
                "----> Running Sync Job State Routine in thread " + Thread.currentThread().getId());
        final List<JDFJob> jobs = filterUsefulJobs();

        for (JDFJob job : jobs) {
            JDFJob jobUpdated = this.synchronizer.sync(job);
            boolean updateResult = this.jobDataStore.update(jobUpdated);

            if (updateResult) {
                logger.debug(
                        "Job ["
                                + job.getId()
                                + "] was successfully synchronized with its execution ["
                                + job.getExecutionId()
                                + "].");
            } else {
                logger.error(
                        "The state of the job ["
                                + job.getId()
                                + "] was not synchronized with its execution.");
            }
        }
    }

    private List<JDFJob> filterUsefulJobs() {
        return this.jobDataStore.getAll().stream()
                .filter(
                        job ->
                                !(job.getState().equals(JobState.FINISHED)
                                        || job.getState().equals(JobState.FAILED)))
                .collect(Collectors.toList());
    }
}

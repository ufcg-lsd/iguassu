package org.fogbowcloud.app.core.routines;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.datastore.managers.JobDBManager;
import org.fogbowcloud.app.jes.arrebol.Synchronizer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This routine is responsible for synchronizing the state of jobs with the state of their
 * execution.
 */
public class SyncJobStateRoutine implements Runnable {

    private static final Logger logger = Logger.getLogger(SyncJobStateRoutine.class);
    private Synchronizer<Job> synchronizer;

    SyncJobStateRoutine(Synchronizer<Job> synchronizer) {
        this.synchronizer = synchronizer;
    }

    @Override
    public void run() {
        logger.info("----> Running Sync Job State Routine in thread with id [" + Thread.currentThread().getId() + "]");
        final List<Job> jobs = filterUsefulJobs();

        for (Job job : jobs) {
            if (Objects.nonNull(job.getExecutionId()) && !job.getExecutionId().trim().isEmpty()) {

                try {
                    this.synchronizer.sync(job);
                    logger.info("Job [" + job.getId() + "] was successfully synchronized with its execution ["
                            + job.getExecutionId() + "].");
                } catch (Exception e) {
                    logger.error("The state of the job [" + job.getId() + "] was not synchronized with its execution.");
                }
            } else {
                logger.debug("Job with id [" + job.getId() + "] has no associated execution.");
            }
        }
    }

    private List<Job> filterUsefulJobs() {
        return JobDBManager.getInstance().findAll().stream()
                .filter(job -> !(job.getState().equals(JobState.FINISHED)
                        || job.getState().equals(JobState.FAILED))).collect(Collectors.toList());
    }
}

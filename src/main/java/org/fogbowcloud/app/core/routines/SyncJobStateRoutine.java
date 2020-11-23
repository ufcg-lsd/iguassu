package org.fogbowcloud.app.core.routines;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.managers.JobDBManager;
import org.fogbowcloud.app.core.datastore.managers.QueueDBManager;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.core.models.queue.ArrebolQueue;
import org.fogbowcloud.app.jes.arrebol.Synchronizer;
import org.fogbowcloud.app.utils.Pair;

/**
 * This routine is responsible for synchronizing the state of jobs with the state of their
 * execution.
 */
public class SyncJobStateRoutine extends Routine implements Runnable {

    private static final Logger logger = Logger.getLogger(SyncJobStateRoutine.class);
    private Synchronizer<Pair<String, Job>> synchronizer;

    SyncJobStateRoutine(long id, String name, Synchronizer<Pair<String, Job>> synchronizer) {
        super(id, name);
        this.synchronizer = synchronizer;
    }

    @Override
    public void run() {
        logger.info("Running routine " + this.name + ".");
        final List<Pair<String, Job>> jobs = filterUsefulJobs();

        for (Pair<String, Job> pair : jobs) {
            Job job = pair.getValue();
            if (Objects.nonNull(job.getExecutionId()) && !job.getExecutionId().trim().isEmpty()) {

                try {
                    this.synchronizer.sync(pair);
                    JobDBManager.getInstance().save(job);
                    logger.info("Job [" + job.getId()
                        + "] was successfully synchronized with its execution [" + job
                        .getExecutionId() + "].");
                } catch (Exception e) {
                    logger.error("The state of the job [" + job.getId()
                        + "] was not synchronized with its execution.");
                }
            } else {
                logger.error("Job with id [" + job.getId() + "] has no associated execution.");
            }
        }
    }

    private List<Pair<String, Job>> filterUsefulJobs() {
        List<Pair<String, Job>> jobs = new ArrayList<>();
        List<ArrebolQueue> queues = QueueDBManager.getInstance().findAll();
        for (ArrebolQueue queue : queues) {
            queue.getJobs().stream().filter(job -> !(job.getState().equals(JobState.FINISHED) || job.getState()
                .equals(JobState.REMOVED)) || job.getState().equals(JobState.FAILED))
                .forEach((job) -> {
                    Pair<String, Job> p = new Pair<>(queue.getQueueId(), job);
                    jobs.add(p);
                });

        }
        return jobs;
    }
}

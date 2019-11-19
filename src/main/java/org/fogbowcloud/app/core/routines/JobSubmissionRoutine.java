package org.fogbowcloud.app.core.routines;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.managers.JobDBManager;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;
import org.fogbowcloud.app.utils.Pair;

import java.util.Objects;
import java.util.Queue;

/**
 * This routine checks from time to time if there are any jobs in the buffer to be submitted to the
 * execution system.
 */
public class JobSubmissionRoutine extends Thread {

    private static final Logger logger = Logger.getLogger(JobSubmissionRoutine.class);

    private JobExecutionService jobExecutionSystem;
    private final Queue<Pair<String, Job>> jobsToSubmit;

    JobSubmissionRoutine(JobExecutionService jobExecutionSystem, Queue<Pair<String, Job>> jobsToSubmit) {
        this.jobExecutionSystem = jobExecutionSystem;
        this.jobsToSubmit = jobsToSubmit;
    }

    @Override
    public void run() {
        while (true) {
            logger.info("Running routine " + this.getName() + ".");
            if (Objects.nonNull(this.jobsToSubmit.peek())) {
                Pair<String, Job> pair = this.jobsToSubmit.poll();
                String queueId = pair.getKey();
                Job job = pair.getValue();
                logger.info("Job found! Starting job submission with id [" + job.getId() + "] to queue [" + queueId + "]");
                try {
                    final String executionId = this.jobExecutionSystem.submit(queueId, job);
                    job.setExecutionId(executionId);
                    job.setState(JobState.QUEUED);
                    JobDBManager.getInstance().save(job);

                    logger.info("Iguassu Job [" + job.getId() + "] has execution id: [" + job.getExecutionId() + "]");

                } catch (ArrebolConnectException ace) {
                    logger.error("Job execution service is not available right now.");
                    this.jobsToSubmit.offer(pair);
                    break;
                } catch (Exception e) {
                    job.setState(JobState.FAILED);
                    JobDBManager.getInstance().save(job);
                    logger.error("Error submitting job with id: [" + job.getId() + "]. " +
                            "Maybe the Job is poorly formed.", e);
                }
            } else {
                logger.info("Job submission buffer is empty.");
                try {
                    synchronized (this.jobsToSubmit) {
                        logger.debug("Routine sleeping");
                        this.jobsToSubmit.wait();
                    }
                } catch (InterruptedException e) {
                    logger.debug(e.getMessage());
                }
            }
        }
    }
}

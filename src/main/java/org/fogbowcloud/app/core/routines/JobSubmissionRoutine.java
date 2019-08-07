package org.fogbowcloud.app.core.routines;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.datastore.managers.JobDBManager;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;

import java.util.Objects;
import java.util.Queue;

/**
 * This routine checks from time to time if there are any jobs in the buffer to be submitted to the
 * execution system.
 */
public class JobSubmissionRoutine implements Runnable {

    private static final Logger logger = Logger.getLogger(JobSubmissionRoutine.class);

    private JobExecutionService jobExecutionSystem;
    private Queue<Job> jobsToSubmit;

    JobSubmissionRoutine(JobExecutionService jobExecutionSystem, Queue<Job> jobsToSubmit) {
        this.jobExecutionSystem = jobExecutionSystem;
        this.jobsToSubmit = jobsToSubmit;
    }

    @Override
    public void run() {
        logger.debug(
                "----> Running Job Submission Routine in thread with id [" + Thread.currentThread().getId() + "]");
        while (Objects.nonNull(this.jobsToSubmit.peek())) {
            Job job = this.jobsToSubmit.poll();
            logger.debug("Job found! Starting job submission with id [" + job.getId() + "]");
            try {
                final String executionId = this.jobExecutionSystem.submit(job);
                job.setExecutionId(executionId);
                job.setState(JobState.SUBMITTED);
                JobDBManager.getInstance().update(job);

                logger.info("Iguassu Job [" + job.getId() + "] has execution id: [" + job.getExecutionId() + "]");

            } catch (ArrebolConnectException ace) {
                logger.error("Job execution service is not available right now.");
                this.jobsToSubmit.offer(job);
                break;
            } catch (Exception e) {
                job.setState(JobState.FAILED);
                JobDBManager.getInstance().update(job);
                logger.error("Error submitting job with id: [" + job.getId() + "]. " +
                                "Maybe the Job is poorly formed.", e);
            }
        }

        if (Objects.isNull(this.jobsToSubmit.peek())) {
            logger.debug("Job submission buffer is empty.");
        }
    }
}

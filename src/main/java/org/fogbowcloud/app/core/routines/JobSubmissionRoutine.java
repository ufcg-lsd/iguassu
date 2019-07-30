package org.fogbowcloud.app.core.routines;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JobState;
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
    private JobDataStore jobDataStore;
    private JobExecutionService jobExecutionSystem;
    private Queue<JDFJob> jobsToSubmit;

    JobSubmissionRoutine(
            JobDataStore jobDataStore,
            JobExecutionService jobExecutionSystem,
            Queue<JDFJob> jobsToSubmit) {
        this.jobDataStore = jobDataStore;
        this.jobExecutionSystem = jobExecutionSystem;
        this.jobsToSubmit = jobsToSubmit;
    }

    @Override
    public void run() {
        logger.debug(
                "----> Running Job Submission Routine in thread with id ["
                        + Thread.currentThread().getId()
                        + "]");
        while (Objects.nonNull(this.jobsToSubmit.peek())) {
            JDFJob job = this.jobsToSubmit.poll();
            logger.debug("Job found! Starting job submission with id [" + job.getId() + "]");
            try {
                final String executionId = this.jobExecutionSystem.submit(job);
                job.setExecutionId(executionId);
                job.setState(JobState.SUBMITTED);
                this.jobDataStore.update(job);

                logger.info(
                        "Iguassu Job ["
                                + job.getId()
                                + "] has execution id: ["
                                + job.getExecutionId()
                                + "]");

            } catch (ArrebolConnectException ace) {
                logger.error("Job execution service is not available right now.");
                this.jobsToSubmit.offer(job);
                break;
            } catch (Exception e) {
                job.setState(JobState.FAILED);
                this.jobDataStore.update(job);
                logger.error(
                        "Error submitting job with id: ["
                                + job.getId()
                                + "]. Maybe the Job is poorly formed.",
                        e);
            }
            JDFJob resultJob = this.jobDataStore.getByJobId(job.getId(), job.getUserId());
        }


        if (Objects.isNull(this.jobsToSubmit.peek())) {
            logger.debug("Job submission buffer is empty.");
        }
    }
}

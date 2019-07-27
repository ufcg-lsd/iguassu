package org.fogbowcloud.app.core.monitor;

import java.util.Objects;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JobState;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;

public class JobSubmissionMonitor implements Runnable {

    private static final Logger logger = Logger.getLogger(JobSubmissionMonitor.class);
    private JobDataStore jobDataStore;
    private JobExecutionService jobExecutionSystem;
    private Queue<JDFJob> jobsToSubmit;

    JobSubmissionMonitor(
            JobDataStore jobDataStore,
            JobExecutionService jobExecutionSystem,
            Queue<JDFJob> jobsToSubmit) {
        this.jobDataStore = jobDataStore;
        this.jobExecutionSystem = jobExecutionSystem;
        this.jobsToSubmit = jobsToSubmit;
    }

    @Override
    public void run() {
        logger.debug("Checking for jobs to be submitted.");
        while (Objects.nonNull(this.jobsToSubmit.peek())) {
            JDFJob job = this.jobsToSubmit.poll();
            logger.debug("Job found! Starting job submission with id [" + job.getId() + "]");
            try {
                final String arrebolId = this.jobExecutionSystem.submit(job);
                job.setExecutionId(arrebolId);
                job.setState(JobState.SUBMITTED);
                this.jobDataStore.update(job);

                logger.info(
                        "Iguassu Job ["
                                + job.getId()
                                + "] has arrebol id: ["
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
        }
        if (Objects.nonNull(this.jobsToSubmit.peek())) {
            logger.debug("Job submission buffer is empty.");
        }
    }
}

package org.fogbowcloud.app.core.monitor;

import java.util.Objects;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;

public class JobSubmissionMonitor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(JobSubmissionMonitor.class);
    private JobDataStore jobDataStore;
    private JobExecutionService jobExecutionSystem;
    private Queue<JDFJob> jobsBuffer;

    public JobSubmissionMonitor(JobDataStore jobDataStore,
        JobExecutionService jobExecutionSystem,
        Queue<JDFJob> jobsBuffer) {
        this.jobDataStore = jobDataStore;
        this.jobExecutionSystem = jobExecutionSystem;
        this.jobsBuffer = jobsBuffer;
    }

    @Override
    public void run() {
        LOGGER.debug("Checking for jobs to be submitted.");
        JDFJob job = this.jobsBuffer.peek();
        if (Objects.nonNull(job)) {
            LOGGER.debug("Job found! Starting job submission with id [" + job.getId() + "]");
            try {
                final String arrebolId = this.jobExecutionSystem.execute(job);
                job.setJobIdArrebol(arrebolId);
                job.setState(JDFJobState.SUBMITTED);
                this.jobDataStore.update(job);
                this.jobsBuffer.poll();
                LOGGER.info(
                    "Iguassu Job [" + job.getId() + "] has arrebol id: [" + job.getJobIdArrebol()
                        + "]");

            } catch (ArrebolConnectException ace) {
                LOGGER.error("Job execution service is not available right now.");
            } catch (Exception e) {
                job.setState(JDFJobState.FAILED);
                this.jobDataStore.update(job);
                this.jobsBuffer.poll();
                LOGGER.error("Error submitting job with id: [" + job.getId()
                    + "]. Maybe the Job is poorly formed.", e);
            }
        } else {
            LOGGER.debug("Job submission buffer is empty.");
        }
    }
}
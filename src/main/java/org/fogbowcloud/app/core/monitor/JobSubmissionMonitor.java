package org.fogbowcloud.app.core.monitor;

import java.util.Objects;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jes.JobExecutionSystem;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;

public class JobSubmissionMonitor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(JobSubmissionMonitor.class);
    private JobDataStore jobDataStore;
    private JobExecutionSystem jobExecutionSystem;
    private Queue<JDFJob> jobsBuffer;

    public JobSubmissionMonitor(JobDataStore jobDataStore,
        JobExecutionSystem jobExecutionSystem,
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
                final String arrebolId = jobExecutionSystem.execute(job);
                job.setJobIdArrebol(arrebolId);
                jobDataStore.update(job);
                jobsBuffer.poll();
                LOGGER.info(
                    "Iguassu Job [" + job.getId() + "] has arrebol id: [" + job.getJobIdArrebol()
                        + "]");

            } catch (ArrebolConnectException ace) {
                LOGGER.error(ace.getMessage(), ace);
            } catch (Exception e) {
                job.setState(JDFJobState.FAILED);
                jobDataStore.update(job);
                jobsBuffer.poll();
                LOGGER.error("Error submitting job with id: [" + job.getId()
                    + "]. Maybe the Job is poorly formed.", e);
            }
        } else {
            LOGGER.debug("Job submission buffer is empty.");
        }
    }
}

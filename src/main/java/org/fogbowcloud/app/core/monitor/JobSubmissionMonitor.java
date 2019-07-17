package org.fogbowcloud.app.core.monitor;

import java.util.List;
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
    private List<JDFJob> jobsToSubmit;

    public JobSubmissionMonitor(JobDataStore jobDataStore,
        JobExecutionSystem jobExecutionSystem,
        List<JDFJob> jobsToSubmit) {
        this.jobDataStore = jobDataStore;
        this.jobExecutionSystem = jobExecutionSystem;
        this.jobsToSubmit = jobsToSubmit;
    }

    @Override
    public void run() {
        for (JDFJob job : jobsToSubmit) {
            try {
                String arrebolId = jobExecutionSystem.execute(job);
                job.setJobIdArrebol(arrebolId);
                jobDataStore.update(job);
                jobsToSubmit.remove(job);
                LOGGER.info(
                    "Iguassu Job [" + job.getId() + "] has arrebol id: [" + job.getJobIdArrebol()
                        + "]");
            } catch (ArrebolConnectException ace){
                LOGGER.error(ace.getMessage(), ace);
            } catch (Exception e) {
                job.setState(JDFJobState.FAILED);
                jobDataStore.update(job);
                jobsToSubmit.remove(job);
                LOGGER.error("Error while submitting job with id: [" + job.getId() + "]", e);
            }
        }
    }

}

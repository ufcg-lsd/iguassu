package org.fogbowcloud.app.jes;

import org.fogbowcloud.app.jes.arrebol.models.JobExecArrebol;
import org.fogbowcloud.app.core.models.job.Job;

/**
 * Interface that defines job execution operations.
 */
public interface JobExecutionService {

    /**
     * Submit a such Job passed by params and creates an <strong>execution</strong>.
     *
     * @param job to be submitted for execution.
     * @return an execution identifier.
     * @throws Exception If any part of the operation goes wrong, be it submission to the Execution
     *                   Service or manipulation of some intermediate object.
     */
    String submit(String queueId, Job job) throws Exception;

    /**
     * Queries the state of the execution and represent as a JobState.
     *
     * @param executionId to be queried in the Execution Service.
     * @return the current {@link JobExecArrebol} state for the refer execution.
     */
    JobExecArrebol status(String queueId, String executionId);
}

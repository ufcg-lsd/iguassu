package org.fogbowcloud.app.jes;

import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JobState;

/** Interface that defines job execution operations. */
public interface JobExecutionService {

    /**
     * Creates an execution for the job submitted in params.
     *
     * @param job to be submitted for execution.
     * @return an execution identifier.
     * @throws Exception If any part of the operation goes wrong, be it submission to the Execution
     *     Service or manipulation of some intermediate object.
     */
    String create(JDFJob job) throws Exception;

    /**
     * Queries the state of the execution and represent as a JobState.
     *
     * @param executionId to be queried in the Execution Service.
     * @return the current JobState for the refer execution.
     */
    JobState status(String executionId);
}

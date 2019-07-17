package org.fogbowcloud.app.jes;

import javax.print.attribute.standard.JobState;
import org.fogbowcloud.app.core.dto.arrebol.ArrebolJobDTO;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.exceptions.SubmitJobException;

public interface JobExecutionSystem {

    String execute(JDFJob job) throws Exception;

    ArrebolJobDTO getJob(String jobId);

    JobState jobState(String executionId);

    void stop(String jobId);
}
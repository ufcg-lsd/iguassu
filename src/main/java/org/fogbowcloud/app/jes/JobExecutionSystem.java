package org.fogbowcloud.app.jes;

import org.fogbowcloud.app.core.dto.JobDTO;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

import javax.print.attribute.standard.JobState;

public interface JobExecutionSystem {

    String execute(JDFJob job);

    JobDTO getJob(String jobId);

    JobState jobState(String executionId);

    void stop(String jobId);
}
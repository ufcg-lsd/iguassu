package org.fogbowcloud.app.jes;

import org.fogbowcloud.app.jdfcompiler.job.Job;

public interface JobExecutionSystem {

    void execute(Job job);

    Job getCurrentState();

    void stop(String jobId);
}
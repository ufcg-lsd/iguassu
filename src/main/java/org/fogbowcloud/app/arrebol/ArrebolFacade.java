package org.fogbowcloud.app.arrebol;

import org.fogbowcloud.app.jdfcompiler.job.Job;

public class ArrebolFacade {

    private ArrebolController arrebolController;

    public ArrebolFacade() {
        this.arrebolController = new ArrebolController();
    }

    public void executeJob(Job job) {
        this.arrebolController.executeJob(job);
    }

    public Job getJobState(String jobId) {
        return null;
    }

    public void stopJob(String jobId) {

    }
}

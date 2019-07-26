package org.fogbowcloud.app.jdfcompiler.job;

public enum JobState {
    SUBMITTED("Submitted"),
    QUEUED("Queued"),
    FAILED("Failed"),
    CREATED("Created"),
    FINISHED("Finished"),
    RUNNING("Running"),
    WAITING("Waiting");

    private String state;

    JobState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }
}

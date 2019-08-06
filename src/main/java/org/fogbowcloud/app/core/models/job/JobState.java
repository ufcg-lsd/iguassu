package org.fogbowcloud.app.core.models.job;

public enum JobState {
    SUBMITTED("Submitted"),
    QUEUED("Queued"),
    FAILED("Failed"),
    CREATED("Created"),
    FINISHED("Finished"),
    RUNNING("Running");

    private String state;

    JobState(String state) {
        this.state = state;
    }

    public String getValue() {
        return this.state;
    }

}

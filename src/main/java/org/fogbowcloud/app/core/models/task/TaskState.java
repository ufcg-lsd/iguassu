package org.fogbowcloud.app.core.models.task;

public enum TaskState {
    READY("Ready"),
    RUNNING("Running"),
    FINISHED("Finished"),
    FAILED("Failed"),
    PENDING("Pending");

    private String description;

    TaskState(String description) {
        this.description = description;
    }

    public static TaskState getTaskStateFromDesc(String desc) {
        for (TaskState ts : values()) {
            if (ts.getDescription().equals(desc)) {
                return ts;
            }
        }
        return null;
    }

    public String getDescription() {
        return this.description;
    }
}

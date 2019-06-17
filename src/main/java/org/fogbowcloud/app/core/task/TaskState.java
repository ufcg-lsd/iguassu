package org.fogbowcloud.app.core.task;

public enum TaskState {

    READY("Ready"),
    RUNNING("Running"),
    FINISHED("Finished"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    NOT_CREATED("Not Created"),
    TIMEDOUT("Timedout"),
    PENDING("Pending");

    private String description;

    TaskState(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

    public static TaskState getTaskStateFromDesc(String desc) {
        for (TaskState ts : values()) {
            if(ts.getDescription().equals(desc)){
                return ts;
            }
        }
        return null;
    }
}

package org.fogbowcloud.app.jes.arrebol.models;

import com.google.gson.annotations.SerializedName;

public class ArrebolTask {

    private String id;

    private ArrebolTaskState state;

    @SerializedName("tasks_specs")
    private ArrebolTaskSpec taskSpec;

    public ArrebolTask(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public ArrebolTaskState getState() {
        return this.state;
    }

    public void setState(ArrebolTaskState state) {
        this.state = state;
    }

    public ArrebolTaskSpec getTaskSpec() {
        return this.taskSpec;
    }

    public void setTaskSpec(ArrebolTaskSpec taskSpec) {
        this.taskSpec = taskSpec;
    }
}

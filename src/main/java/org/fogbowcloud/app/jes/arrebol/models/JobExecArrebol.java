package org.fogbowcloud.app.jes.arrebol.models;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class JobExecArrebol {

    private String id;

    @SerializedName("job_state")
    private ExecutionState state;

    private String label;

    private List<ArrebolTask> tasks;

    public JobExecArrebol() {}

    public JobExecArrebol(String label, Collection<ArrebolTask> tasks) {
        this.label = label;
        this.state = ExecutionState.SUBMITTED;
        this.tasks = new LinkedList<>(tasks);
    }

    public String getId() {
        return this.id;
    }

    public ExecutionState getState() {
        return this.state;
    }

    public List<ArrebolTask> getTasks() {
        return new LinkedList<>(this.tasks);
    }
}

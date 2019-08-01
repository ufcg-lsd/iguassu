package org.fogbowcloud.app.jes.arrebol.models;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ArrebolJob {

    private String id;

    private ExecutionState executionState;

    private List<ArrebolTask> tasks;

    public ArrebolJob(String label, Collection<ArrebolTask> tasks) {
        this.executionState = ExecutionState.SUBMITTED;
        this.tasks = new LinkedList<>(tasks);
    }

    public String getId() {
        return this.id;
    }

    public ExecutionState getExecutionState() {
        return this.executionState;
    }

    public List<ArrebolTask> getTasks() {
        return new LinkedList<>(this.tasks);
    }
}

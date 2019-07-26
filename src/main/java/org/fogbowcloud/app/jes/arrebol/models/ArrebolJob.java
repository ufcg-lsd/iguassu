package org.fogbowcloud.app.jes.arrebol.models;

import java.util.*;

public class ArrebolJob {

    private String id;

    private ExecutionState jobState;

    private List<ArrebolTask> tasks;

    public ArrebolJob(String label, Collection<ArrebolTask> tasks){
        this.jobState = ExecutionState.SUBMITTED;
        this.tasks = new LinkedList<>(tasks);
    }

    public String getId(){
        return this.id;
    }

    public ExecutionState getJobState(){
        return this.jobState;
    }

    public void setJobState(ExecutionState jobState){
        this.jobState = jobState;
    }

    public List<ArrebolTask> getTasks(){
        return new LinkedList<>(this.tasks);
    }
}

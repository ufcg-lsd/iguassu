package org.fogbowcloud.app.jes.arrebol.models;

import java.util.*;

public class ArrebolJob {

    private static final long serialVersionUID = -6111900503095749695L;

    private String id;

    private ArrebolJobState jobState;

    private List<ArrebolTask> tasks;

    public ArrebolJob(String label, Collection<ArrebolTask> tasks){
        this.jobState = ArrebolJobState.SUBMITTED;
        this.tasks = new LinkedList<>(tasks);
    }

    public String getId(){
        return this.id;
    }

    public ArrebolJobState getJobState(){
        return this.jobState;
    }

    public void setJobState(ArrebolJobState jobState){
        this.jobState = jobState;
    }

    public List<ArrebolTask> getTasks(){
        return new LinkedList<>(this.tasks);
    }
}

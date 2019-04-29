package org.fogbowcloud.app.jes.arrebol.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArrebolJob {

    private static final long serialVersionUID = -6111900503095749695L;

    private String id;

    private ArrebolJobState jobState;

    private Map<String, ArrebolTask> tasks;

    public ArrebolJob(String label, Collection<ArrebolTask> tasks){
        this.id = UUID.randomUUID().toString();
        this.jobState = ArrebolJobState.SUBMITTED;

        this.tasks = new HashMap<>();

        for(ArrebolTask task: tasks) {
            this.tasks.put(task.getId(), task);
        }
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

    public Map<String, ArrebolTask> getTasks(){
        Map<String, ArrebolTask> mapTasks = new HashMap<>(this.tasks);
        return mapTasks;
    }
}

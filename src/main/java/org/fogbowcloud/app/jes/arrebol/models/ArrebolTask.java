package org.fogbowcloud.app.jes.arrebol.models;


public class ArrebolTask {
    private String id;

    private ArrebolTaskState state;

    public ArrebolTask(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public ArrebolTaskState getState() {
        return state;
    }

    public void setState(ArrebolTaskState state) {
        this.state = state;
    }
}

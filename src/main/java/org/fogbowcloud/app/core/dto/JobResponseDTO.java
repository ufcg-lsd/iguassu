package org.fogbowcloud.app.core.dto;

import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JobResponseDTO {

    private String id;
    private String label;
    private String creationDate;
    private JobState state;

    public JobResponseDTO(Job job) {
        setFields(job);
    }

    private void setFields(Job job) {
        this.id = job.getId();
        this.label = job.getLabel();
        this.creationDate = timestampToDate(job.getTimestamp());
        this.state = job.getState();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public String getCreationDate() {
        return this.creationDate;
    }

    private String timestampToDate(long timestamp) {
        Date date = new java.util.Date(timestamp * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-3"));
        return sdf.format(date);
    }
}

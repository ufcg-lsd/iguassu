package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Data transfer object projection */
public class JobDTO implements Serializable  {

    private String id;

    private String label;

    @JsonProperty("queue_id")
    private String queueId;

    @JsonProperty("creation_date")
    private String creationDate;

    private JobState state;

    @JsonProperty("owner_id")
    private Long ownerId;

    public JobDTO(Job job, String queueId) {
        this.queueId = queueId;
        setFields(job);
    }

    private void setFields(Job job) {
        this.id = job.getId();
        this.ownerId = job.getOwnerId();
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

    private String timestampToDate(Long timestamp) {
        Date date = new java.util.Date(timestamp * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-3"));
        return sdf.format(date);
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}

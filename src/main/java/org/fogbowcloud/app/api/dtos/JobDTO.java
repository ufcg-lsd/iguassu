package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Data transfer object projection */
public class JobDTO implements Serializable  {

    @ApiModelProperty(notes = "The job id", position = 1, example = "3eec48d3-b387-4610-ac4c-2c1b006deeb9")
    private String id;

    @ApiModelProperty(notes = "The job label", position = 2, example = "job_label")
    private String label;

    @ApiModelProperty(notes = "The Queue ID", position = 3, example = "d73b30b9-18b9-4274-b42a-e96a6f001458")
    @JsonProperty("queue_id")
    private String queueId;

    @ApiModelProperty(notes = "The job creation date", position = 4, example = "2020/11/10 15:36:54")
    @JsonProperty("creation_date")
    private String creationDate;

    @ApiModelProperty(notes = "The job state", position = 5, example = "RUNNING")
    private JobState state;

    @ApiModelProperty(notes = "The ID of the user who submitted the job", position = 6, example = "1")
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

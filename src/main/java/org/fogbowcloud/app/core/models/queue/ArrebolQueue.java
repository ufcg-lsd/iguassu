package org.fogbowcloud.app.core.models.queue;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.fogbowcloud.app.core.models.job.Job;

@Entity
public class ArrebolQueue {

    @Id
    private String queueId;

    private Long ownerId;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = Job.class)
    private List<Job> jobs;

    public ArrebolQueue(String queueId, Long ownerId, List<Job> jobs) {
        this.queueId = queueId;
        this.ownerId = ownerId;
        this.jobs = jobs;
    }

    public ArrebolQueue() {
    }

    public String getQueueId() {
        return queueId;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}

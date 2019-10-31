package org.fogbowcloud.app.core.models.queue;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.fogbowcloud.app.core.models.job.Job;

@Entity
public class Queue {

    @Id
    private String queueId;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = Job.class)
    private List<Job> jobs;

    public Queue(String queueId, List<Job> jobs) {
        this.queueId = queueId;
        this.jobs = jobs;
    }

    public Queue() {
    }

    public String getQueueId() {
        return queueId;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}

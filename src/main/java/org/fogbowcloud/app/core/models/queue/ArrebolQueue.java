package org.fogbowcloud.app.core.models.queue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fogbowcloud.app.core.models.job.Job;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ArrebolQueue {

    @Id
    private String queueId;

    private String name;

    private Long ownerId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> pool;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = Job.class)
    @Fetch(FetchMode.SUBSELECT)
    private List<Job> jobs;

    public ArrebolQueue(String queueId, Long ownerId, List<Job> jobs, String name, List<String> pool) {
        this.queueId = queueId;
        this.ownerId = ownerId;
        this.jobs = jobs;
        this.pool = pool;
        this.name = name;
    }

    public ArrebolQueue() {}

    public String getQueueId() {
        return queueId;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public List<String> getPool() {
        return pool;
    }

    public void setPool(List<String> pool) {
        this.pool = pool;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized void addNode(String address) {
        this.pool.add(address);
    }
}

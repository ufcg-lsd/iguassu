package org.fogbowcloud.app.jes.arrebol.dtos;

import com.google.gson.annotations.SerializedName;

public class QueueDTO {

    public String id;
    public String name;
    @SerializedName("waiting_jobs")
    public long waitingJobs;
    @SerializedName("workers_nodes")
    public Integer workersNodes;
    @SerializedName("worker_pool")
    public Integer workerPool;

    public QueueDTO(String id, String name, long waitingJobs, int workersNodes, int workerPool) {
        this.id = id;
        this.name = name;
        this.waitingJobs = waitingJobs;
        this.workersNodes = workersNodes;
        this.workerPool = workerPool;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getWaitingJobs() {
        return waitingJobs;
    }

    public Integer getWorkersNodes() {
        return workersNodes;
    }

    public Integer getWorkerPool() {
        return workerPool;
    }
}

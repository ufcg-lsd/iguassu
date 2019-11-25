package org.fogbowcloud.app.jes.arrebol.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import javax.validation.constraints.NotEmpty;

public class WorkerNode {

    @NotEmpty
    private String address;

    @JsonProperty("worker_pool")
    @SerializedName("worker_pool")
    private Integer workerPool;

    public WorkerNode() {
    }

    public WorkerNode(String address, Integer workerPool) {
        this.address = address;
        this.workerPool = workerPool;
    }

    public String getAddress() {
        return address;
    }

    public Integer getWorkerPool() {
        return workerPool;
    }
}

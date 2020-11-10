package org.fogbowcloud.app.jes.arrebol.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

public class QueueDTO {

    @ApiModelProperty(notes = "The Queue ID", position = 1, example = "d73b30b9-18b9-4274-b42a-e96a6f001458")
    public String id;

    @ApiModelProperty(notes = "The Queue Name", position = 2, example = "My Queue")
    public String name;

    @ApiModelProperty(notes = "The number of jobs that are not in Finished state or in Failed state.", position = 3, example = "10")
    @JsonProperty("waiting_jobs")
    @SerializedName("waiting_jobs")
    private long waitingJobs;

    @ApiModelProperty(notes = "The number of hosts used as workers.", position = 4, example = "10")
    @JsonProperty("nodes")
    @SerializedName("worker_pools")
    private Integer workerPools;

    @ApiModelProperty(notes = "The number of workers available for the queue.", position = 5, example = "30")
    @JsonProperty("workers")
    @SerializedName("pools_size")
    private Integer poolsSize;

    public QueueDTO(String id, String name, long waitingJobs, int workerPools, int poolsSize) {
        this.id = id;
        this.name = name;
        this.waitingJobs = waitingJobs;
        this.workerPools = workerPools;
        this.poolsSize = poolsSize;
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

    public Integer getWorkerPools() {
        return workerPools;
    }

    public Integer getPoolsSize() {
        return poolsSize;
    }
}

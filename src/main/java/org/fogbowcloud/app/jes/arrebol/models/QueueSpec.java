package org.fogbowcloud.app.jes.arrebol.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.validation.constraints.NotNull;

public class QueueSpec {

    @NotNull
    private String name;

    @NotNull
    @JsonProperty("worker_nodes")
    @SerializedName("worker_nodes")
    private List<WorkerNode> workerNodes;

    public QueueSpec() {
    }

    public String getName() {
        return name;
    }

    public List<WorkerNode> getWorkerNodes() {
        return workerNodes;
    }
}

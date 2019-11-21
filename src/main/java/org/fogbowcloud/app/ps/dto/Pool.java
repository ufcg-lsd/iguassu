package org.fogbowcloud.app.ps.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pool {

    @SerializedName("name")
    private String poolName;

    @SerializedName("nodes")
    private List<Node> nodes;

    public String getPoolName() {
        return poolName;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}

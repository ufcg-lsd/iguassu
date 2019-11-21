package org.fogbowcloud.app.ps.models;

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

    public Node getNode(String nodeAddress) {
        Node node = null;
        for(Node n : nodes) {
            if(n.getAddress().equals(nodeAddress)) {
                node = n;
                break;
            }
        }
        return node;
    }
}

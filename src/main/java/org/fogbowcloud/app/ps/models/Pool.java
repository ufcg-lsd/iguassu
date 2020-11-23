package org.fogbowcloud.app.ps.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class Pool {

    @ApiModelProperty(notes = "The pool name", position = 1, example = "default")
    @SerializedName("name")
    private String poolName;

    @ApiModelProperty(notes = "The nodes of pool", position = 2, dataType = "Node")
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

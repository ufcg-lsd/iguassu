package org.fogbowcloud.app.ps.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Pool {

    private String id;

    @SerializedName("name")
    private String poolName;

    @SerializedName("nodes")
    private Map<String, Node> nodes;

    public String getId() {
        return id;
    }

    public String getPoolName() {
        return poolName;
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public Node getNode(String nodeAddress) {
//        Node node = null;
//        for(Node n : nodes) {
//            if(n.getAddress().equals(nodeAddress)) {
//                node = n;
//                break;
//            }
//        }
//        return node;
        return null;
    }
}

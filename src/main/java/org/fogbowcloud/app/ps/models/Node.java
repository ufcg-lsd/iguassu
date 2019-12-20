package org.fogbowcloud.app.ps.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Node {

    private String driver;

    private Map<String, String> spec;

    @SerializedName("ip")
    private String address;

//    @SerializedName("state")
    private String state;

    public String getAddress() {
        return address;
    }

    public String getState() {
        return state;
    }

    @JsonIgnore
    public boolean isReady() {
        return state.equals("READY");
    }

    @JsonIgnore
    public boolean isFailed() {
        return state.equals("FAILED");
    }
}

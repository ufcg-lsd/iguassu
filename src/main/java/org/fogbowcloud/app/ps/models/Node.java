package org.fogbowcloud.app.ps.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class Node {

    @JsonProperty("resource_address")
    @SerializedName("ip")
    private String address;

    @SerializedName("state")
    private String state;

    public String getAddress() {
        return address;
    }

    public String getState() {
        return state;
    }

    @JsonIgnore
    public boolean isProvisioned() {
        return state.equals("provisioned");
    }

    @JsonIgnore
    public boolean isFailed() {
        return state.equals("failed");
    }
}

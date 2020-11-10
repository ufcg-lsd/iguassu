package org.fogbowcloud.app.ps.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

public class Node {

    @ApiModelProperty(notes = "The address of node", position = 1, example = "10.30.1.1")
    @JsonProperty("resource_address")
    @SerializedName("ip")
    private String address;

    @ApiModelProperty(notes = "The node state", position = 2, example = "provisioned")
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

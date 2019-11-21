package org.fogbowcloud.app.ps.dto;

import com.google.gson.annotations.SerializedName;

public class Node {

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
}

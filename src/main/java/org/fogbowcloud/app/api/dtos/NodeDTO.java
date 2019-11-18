package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class NodeDTO implements Serializable {

    private String address;

    @JsonProperty("provision_status")
    private String provisionStatus;

    public NodeDTO() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProvisionStatus() {
        return provisionStatus;
    }

    public void setProvisionStatus(String provisionStatus) {
        this.provisionStatus = provisionStatus;
    }
}

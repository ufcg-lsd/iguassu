package org.fogbowcloud.app.api.dtos;

import java.io.Serializable;

public class NodeDTO implements Serializable {

    private String address;
    private String status;

    public NodeDTO() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

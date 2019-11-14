package org.fogbowcloud.app.api.dtos;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class NodeRequest implements Serializable {

    @NotEmpty
    @NotNull
    private String address;

    public NodeRequest() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

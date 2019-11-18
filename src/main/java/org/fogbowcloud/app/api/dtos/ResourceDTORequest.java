package org.fogbowcloud.app.api.dtos;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class ResourceDTORequest implements Serializable {

    @NotBlank
    private String address;

    public ResourceDTORequest() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

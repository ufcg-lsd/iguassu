package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class ResourceNode implements Serializable {

    @NotBlank
    @JsonProperty("resource_address")
    @SerializedName("resource_address")
    private String resourceAddress;

    @NotNull
    @JsonProperty("pool_size")
    @SerializedName("pool_size")
    private String poolSize;

    public ResourceNode() {}

    public String getResourceAddress() {
        return resourceAddress;
    }

    public String getPoolSize() {
        return poolSize;
    }

    public void setResourceAddress(String resourceAddress) {
        this.resourceAddress = resourceAddress;
    }
}

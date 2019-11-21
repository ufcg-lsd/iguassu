package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class ResourceNode implements Serializable {

    @NotBlank
    @JsonProperty("resource_address")
    private String resourceAddress;

    @NotNull
    @JsonProperty("pool_size")
    private String poolSize;

    public ResourceNode() {}

    public String getResourceAddress() {
        return resourceAddress;
    }

    public String getPoolSize() {
        return poolSize;
    }
}

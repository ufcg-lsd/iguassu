package org.fogbowcloud.app.api.dtos;

import java.io.Serializable;
import java.util.Map;
import javax.validation.constraints.NotBlank;

public class ResourceNode implements Serializable {

    @NotBlank
    private String driver;

    private String template;

    private Map<String, String> spec;

    public ResourceNode() {
    }

    public String getDriver() {
        return driver;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, String> getSpec() {
        return spec;
    }
}

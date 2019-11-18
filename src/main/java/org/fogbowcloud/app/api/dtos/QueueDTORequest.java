package org.fogbowcloud.app.api.dtos;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class QueueDTORequest implements Serializable {

    @NotBlank
    private String name;

    public QueueDTORequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

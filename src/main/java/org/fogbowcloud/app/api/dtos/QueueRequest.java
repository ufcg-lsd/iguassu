package org.fogbowcloud.app.api.dtos;

import java.io.Serializable;

public class QueueRequest implements Serializable {

    private String name;

    public QueueRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

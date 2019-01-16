package org.fogbowcloud.app.exception;

import org.fogbowcloud.app.model.Status;

public class ResourceException extends RuntimeException {
    private Status status;

    public ResourceException(Status status, String message) {
        this(message);
        this.status = status;
    }

    public ResourceException(String message) {
        super(message);
    }

    public Status getStatus() {
        return status;
    }
}

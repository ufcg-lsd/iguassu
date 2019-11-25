package org.fogbowcloud.app.jes.exceptions;

public class QueueCreationException extends RuntimeException {

    public QueueCreationException(String message, Exception e) {
        super(message, e);
    }
}

package org.fogbowcloud.app.core.exceptions;

/** Appropriate exception for when any data storage access operation fails. */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.fogbowcloud.app.core.exceptions;

public class InvalidParameterException extends IguassuException {

    private static final String DEFAULT_MESSAGE = "Invalid parameter exceptions";

    public InvalidParameterException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

}

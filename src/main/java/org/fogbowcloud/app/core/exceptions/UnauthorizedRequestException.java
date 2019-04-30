package org.fogbowcloud.app.core.exceptions;

public class UnauthorizedRequestException extends IguassuException {

    private static final String DEFAULT_MESSAGE = "Unauthorized Error";

    public UnauthorizedRequestException() {
        super(DEFAULT_MESSAGE);
    }

    public UnauthorizedRequestException(String message) {
        super(message);
    }

    public UnauthorizedRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
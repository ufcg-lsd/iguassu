package org.fogbowcloud.app.exception;

public class UnauthorizedRequestException extends ArrebolException {

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

package org.fogbowcloud.app.core.exceptions;

/**
 * Appropriate exception when an unauthorized request is made, for example, requests with invalid
 * Token Iguassu.
 */
public class UnauthorizedRequestException extends IguassuException {

    public UnauthorizedRequestException(String message) {
        super(message);
    }
}

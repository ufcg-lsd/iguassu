package org.fogbowcloud.app.core.exceptions;

/**
 * Appropriate exception for when the authorization code used is incorrect or has already been used.
 */
public class InvalidAuthorizationCodeException extends IguassuException {
    private static final String DEFAULT_MESSAGE = "Invalid authorization code.";

    public InvalidAuthorizationCodeException() {
        super(DEFAULT_MESSAGE);
    }
}

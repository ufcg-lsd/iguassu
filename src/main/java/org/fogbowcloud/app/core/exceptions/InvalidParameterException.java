package org.fogbowcloud.app.core.exceptions;

/** Appropriate exception for when any invalid, null, or malformed fields are used. */
public class InvalidParameterException extends IguassuException {

    public InvalidParameterException(String message) {
        super(message);
    }
}

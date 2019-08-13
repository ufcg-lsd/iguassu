package org.fogbowcloud.app.core.exceptions;

/**
 * Appropriate exception when an query operation of search a such user don't find anyone.
 */
public class UserNotExistException extends IguassuException {

    public UserNotExistException(String msg) {
        super(msg);
    }
}

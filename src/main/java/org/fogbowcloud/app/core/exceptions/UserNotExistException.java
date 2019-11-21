package org.fogbowcloud.app.core.exceptions;

/**
 * Appropriate exception when an query operation of search a such user don't find anyone.
 */
public class UserNotExistException extends IguassuException {

    private static final String ERR_MSG_DEFAULT = "The user could not be recovered because it has not yet been stored.";

    public UserNotExistException(String msg) {
        super(msg);
    }

    public UserNotExistException() {
        super(ERR_MSG_DEFAULT);
    }
}

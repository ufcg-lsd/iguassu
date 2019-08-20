package org.fogbowcloud.app.core.exceptions;

import org.apache.http.conn.HttpHostConnectException;

/**
 * Appropriate exception when any attempt to connect to the authentication and storage service fails
 * for connection reasons.
 */
public class StorageServiceConnectException extends RuntimeException {

    public StorageServiceConnectException(String s, HttpHostConnectException e) {
        super(s, e);
    }
}

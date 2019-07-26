package org.fogbowcloud.app.api.exceptions;

import org.apache.http.conn.HttpHostConnectException;

public class StorageServiceConnectException extends RuntimeException {

    public StorageServiceConnectException(String s, HttpHostConnectException e) {
        super(s, e);
    }
}

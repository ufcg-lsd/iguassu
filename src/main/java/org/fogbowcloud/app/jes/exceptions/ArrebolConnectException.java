package org.fogbowcloud.app.jes.exceptions;

import org.apache.http.conn.HttpHostConnectException;

public class ArrebolConnectException extends RuntimeException {

    public ArrebolConnectException(String s, HttpHostConnectException e) {
        super(s, e);
    }
}

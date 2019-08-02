package org.fogbowcloud.app.jes.exceptions;

import org.apache.http.conn.HttpHostConnectException;

/** Appropriate exception for when the connection with the Execution Service is down. */
public class ArrebolConnectException extends RuntimeException {

    public ArrebolConnectException(String s, HttpHostConnectException e) {
        super(s, e);
    }
}

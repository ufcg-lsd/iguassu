package org.fogbowcloud.app.api.exceptions;

public class NotFoundAccessToken extends RuntimeException {

    public NotFoundAccessToken(String msg) {
        super(msg);
    }
}

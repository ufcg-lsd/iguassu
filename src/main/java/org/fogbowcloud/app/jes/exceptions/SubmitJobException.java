package org.fogbowcloud.app.jes.exceptions;

public class SubmitJobException extends RuntimeException {

    public SubmitJobException(String s, Exception e) {
        super(s, e);
    }
}

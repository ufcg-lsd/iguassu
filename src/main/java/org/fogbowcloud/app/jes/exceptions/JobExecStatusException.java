package org.fogbowcloud.app.jes.exceptions;

/** Appropriate exception for when an attempt to get the status of a job execution fails. */
public class JobExecStatusException extends RuntimeException {

    public JobExecStatusException(String s) {
        super(s);
    }
}

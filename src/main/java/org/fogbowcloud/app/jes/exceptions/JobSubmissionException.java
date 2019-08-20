package org.fogbowcloud.app.jes.exceptions;

/** Appropriate exception for when an attempt to submit a job to execution fails. */
public class JobSubmissionException extends RuntimeException {

    public JobSubmissionException(String s, Exception e) {
        super(s, e);
    }
}

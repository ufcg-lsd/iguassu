package org.fogbowcloud.app.core.models.user;

/**
 * Possible states of a User Session. A Session is a valid time that the user can make requests to
 * the Iguassu.
 */
public enum SessionState {
    ACTIVE("active"),
    EXPIRED("expired");

    private String state;

    SessionState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }
}

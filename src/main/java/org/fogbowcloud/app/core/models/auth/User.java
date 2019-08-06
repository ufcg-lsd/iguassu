package org.fogbowcloud.app.core.models.auth;

/** This interface defines the contract of operations that a user needs to have. */
public interface User {

    /** Retrieves the user identifier; must be non-null. It's unique and should not be mutated. */
    String getIdentifier();

    /** Replaces the current secret token by a new. */
    void updateToken(String token);

    /** Retrieves the current secret token if the user already has one; must be non-null. */
    String getIguassuToken();

    /** True if the user session is not expired. */
    boolean isActive();

    /** Returns the value of the last session start in epoch seconds format. */
    long getSessionTime();

    /** Modifies the state of the session of the user. */
    void changeSessionState(SessionState state);

    /** Sets the current time (now) in epoch seconds format as the session time. */
    void resetSession();
}

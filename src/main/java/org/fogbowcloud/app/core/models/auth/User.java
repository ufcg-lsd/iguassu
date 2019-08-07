package org.fogbowcloud.app.core.models.auth;

import org.fogbowcloud.app.utils.TokenEncrypt;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "user")
public class User implements Serializable {

    private static final String NAME_COLUMN_NAME = "name";
    private static final String IGUASSU_TOKEN_COLUMN_NAME = "iguassu_token";
    private static final String STATE_COLUMN_NAME = "state";
    private static final String SESSION_TIME_COLUMN_NAME = "session_time";

    @Column(name = NAME_COLUMN_NAME)
    private final String name;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = IGUASSU_TOKEN_COLUMN_NAME)
    @Convert(converter = TokenEncrypt.class)
    private String iguassuToken;

    @Column(name = STATE_COLUMN_NAME)
    @Enumerated(EnumType.STRING)
    private SessionState sessionState;

    @Column(name = SESSION_TIME_COLUMN_NAME)
    private long sessionTime;

    public User(String name, String iguassuToken) {
        this.name = name;
        this.iguassuToken = iguassuToken;
        this.resetSession();
        this.sessionState = SessionState.ACTIVE;
    }

    /**
     * Retrieves the user identifier; must be non-null. It's unique and should not be mutated.
     */
    public String getName() {
        return this.name;
    }

    /**
     * True if the user session is not expired.
     */
    public boolean isActive() {
        return this.sessionState == SessionState.ACTIVE;
    }

    /**
     * Modifies the state of the session of the user.
     */
    public void changeSessionState(SessionState state) {
        this.sessionState = state;
    }

    /**
     * Retrieves the current secret token if the user already has one; must be non-null.
     */
    public String getIguassuToken() {
        return this.iguassuToken;
    }

    /**
     * Returns the value of the last session start in epoch seconds format.
     */
    public long getSessionTime() {
        return this.sessionTime;
    }

    private void setSessionTime(long sessionTime) {
        this.sessionTime = sessionTime;
    }

    /**
     * Sets the current time (now) in epoch seconds format as the session time.
     */
    public void resetSession() {
        this.sessionTime = Instant.now().getEpochSecond();
        this.sessionState = SessionState.ACTIVE;
    }

    /**
     * Replaces the current secret token by a new.
     */
    public void updateToken(String token) {
        this.iguassuToken = token;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}

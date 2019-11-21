package org.fogbowcloud.app.core.models.user;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "iguassu_user")
public class User {

    private static final String SESSION_STATE_COLUMN_NAME = "session_state";
    private static final String SESSION_TIME_COLUMN_NAME = "session_time";
    private static final String CREDENTIALS_ID_COLUMN_NAME = "credentials_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String alias;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CREDENTIALS_ID_COLUMN_NAME)
    private Credential credentials;

    @Column(name = SESSION_STATE_COLUMN_NAME)
    @Enumerated(EnumType.STRING)
    private SessionState sessionState;

    @Column(name = SESSION_TIME_COLUMN_NAME)
    private Long sessionTime;

    public User() {
    }

    public User(String alias, Credential credentials) {
        this.alias = alias;
        this.credentials = credentials;
        this.resetSession();
        this.sessionState = SessionState.ACTIVE;
    }

    /**
     * Retrieves the user identifier; must be non-null. It's unique and should not be mutated.
     */
    public String getAlias() {
        return this.alias;
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
     * Returns the value of the last session start in epoch seconds format.
     */
    public Long getSessionTime() {
        return this.sessionTime;
    }

    private void setSessionTime(Long sessionTime) {
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
        this.credentials.setIguassuToken(token);
    }

    public Long getId() {
        return id;
    }

    public Credential getCredentials() {
        return credentials;
    }

    public void setCredentials(Credential credentials) {
        this.credentials = credentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                alias.equals(user.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, id);
    }
}

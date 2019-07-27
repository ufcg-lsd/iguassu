package org.fogbowcloud.app.core.monitor;

import java.time.Instant;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.AuthManager;
import org.fogbowcloud.app.core.auth.models.SessionState;
import org.fogbowcloud.app.core.auth.models.User;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;

public class SessionMonitor implements Runnable {

    private static final Logger logger = Logger.getLogger(SessionMonitor.class);
    private final OAuthTokenDataStore oAuthTokenDataStore;
    private final AuthManager authManager;

    SessionMonitor(OAuthTokenDataStore oAuthTokenDataStore, AuthManager authManager) {
        this.oAuthTokenDataStore = oAuthTokenDataStore;
        this.authManager = authManager;
    }

    @Override
    public void run() {
        final long now = Instant.now().getEpochSecond();
        logger.debug("Starting verification of user sessions at time: " + now);
        final long oneHourInSeconds = 3600;
        for (final OAuthToken token : this.oAuthTokenDataStore.getAll()) {
            final User currentUser = this.authManager.retrieve(token.getUserId());

            if (Objects.nonNull(currentUser)) {

                if ((Math.abs((now - currentUser.getSessionTime())) > oneHourInSeconds)
                        && currentUser.isActive()) {
                    logger.debug(
                            "User [ " + currentUser.getIdentifier() + " ] defined as not active.");
                    currentUser.changeSessionState(SessionState.EXPIRED);
                    this.authManager.update(currentUser);
                }
            }
        }
    }
}

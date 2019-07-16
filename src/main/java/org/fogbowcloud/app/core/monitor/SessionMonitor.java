package org.fogbowcloud.app.core.monitor;

import java.time.Instant;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.authenticator.IguassuAuthenticator;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;

public class SessionMonitor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SessionMonitor.class);
    private final OAuthTokenDataStore oAuthTokenDataStore;
    private final IguassuAuthenticator authenticator;

    public SessionMonitor(OAuthTokenDataStore oAuthTokenDataStore,
        IguassuAuthenticator authenticator) {
        this.oAuthTokenDataStore = oAuthTokenDataStore;
        this.authenticator = authenticator;
    }

    @Override
    public void run() {
        final long now = Instant.now().getEpochSecond();
        LOGGER.debug("Starting verification of user sessions at time: " + now);
        final long oneHourInSeconds = 3600;
        for (final OAuthToken token : oAuthTokenDataStore.getAll()) {
            final User currentUser = this.authenticator.getUserByUsername(token.getUserId());

            if (Math.abs((now - currentUser.getSessionTime())) > oneHourInSeconds) {
                LOGGER.debug(
                    "User [ " + currentUser.getUserIdentification() + " ] defined as not active.");
                currentUser.setActive(false);
            }
        }

    }
}

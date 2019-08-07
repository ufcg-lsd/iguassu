package org.fogbowcloud.app.core.routines;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.AuthManager;

import java.time.Instant;

/**
 * This routine checks from time to time if the user has been inactive for a long time. If so, your
 * session is expired and new user authentication is required.
 */
public class SessionVerificationRoutine implements Runnable {

    private static final Logger logger = Logger.getLogger(SessionVerificationRoutine.class);
    private final AuthManager authManager;

    SessionVerificationRoutine(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public void run() {
        logger.debug(
                "----> Running Session Verification Routine in thread with id ["
                        + Thread.currentThread().getId()
                        + "]");
        final long now = Instant.now().getEpochSecond();
        final long oneHourInSeconds = 3600;
//        for (final OAuthToken token : this.oAuthTokenDataStore.getAll()) {
//            final User currentUser = this.authManager.retrieve(token.getUserId());
//
//            if (Objects.nonNull(currentUser)) {
//
//                if ((Math.abs((now - currentUser.getSessionTime())) > oneHourInSeconds)
//                        && currentUser.isActive()) {
//                    logger.debug(
//                            "User [ " + currentUser.getIdentifier() + " ] defined as not active.");
//                    currentUser.changeSessionState(SessionState.EXPIRED);
//                    this.authManager.update(currentUser);
//                }
//            }
//        }
    }
}

package org.fogbowcloud.app.core.routines;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.user.SessionState;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.core.datastore.managers.UserDBManager;

import java.time.Instant;
import java.util.List;

/**
 * This routine checks from time to time if the user has been inactive for a long time. If so, your
 * session is expired and new user authentication is required.
 */
public class SessionVerificationRoutine implements Runnable {

    private static final Logger logger = Logger.getLogger(SessionVerificationRoutine.class);
    private final UserDBManager userDBManager = UserDBManager.getInstance();

    SessionVerificationRoutine() {
    }

    @Override
    public void run() {
        logger.info(
                "----> Running Session Verification Routine in thread with id [" + Thread.currentThread().getId() + "]");
        final long now = Instant.now().getEpochSecond();
        final long oneHourInSeconds = 3600;
        final List<User> allUsers = this.userDBManager.findAll();

        for (final User user : allUsers) {
            if ((Math.abs((now - user.getSessionTime())) > oneHourInSeconds)
                    && user.isActive()) {
                logger.info("User " + user.getAlias() + " was defined as not active.");
                user.changeSessionState(SessionState.EXPIRED);
                this.userDBManager.update(user);
            }
        }
    }
}

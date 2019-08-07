package org.fogbowcloud.app.core.auth;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.auth.*;
import org.fogbowcloud.app.datastore.OAuthTokenDBManager;
import org.fogbowcloud.app.datastore.UserDBManager;
import org.fogbowcloud.app.utils.RandomString;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

/**
 * Default AuthManager implementation
 */
public class DefaultAuthManager implements AuthManager {

    private static final Logger logger = Logger.getLogger(DefaultAuthManager.class);
    private static final int DEFAULT_IGUASSU_TOKEN_LENGTH = 64;

    private AuthRequestsHelper requestsHelper;

    public DefaultAuthManager(Properties properties) {
        this.requestsHelper = new AuthRequestsHelper(properties);
    }

    @Override
    public User authenticate(OAuth2Identifiers oAuth2Identifiers, String authorizationCode) {
        try {
            OAuthToken oAuthToken =
                    this.requestsHelper.getToken(oAuth2Identifiers, authorizationCode);

            User user = UserDBManager.getInstance().findUserByName(oAuthToken.getUserId());

            String iguassuToken;
            if (Objects.nonNull(user)) {
                logger.debug("Found user [" + user.getName() + "]");
                if (user.isActive()) {
                    logger.debug("User [" + user.getName() + "] is active and has a valid Iguassu Token.");
                } else {
                    iguassuToken = this.generateIguassuToken(oAuthToken.getUserId());
                    logger.debug("Generating a new Iguassu Token for the user [" + user.getName() + "].");
                    user.changeSessionState(SessionState.ACTIVE);
                    logger.debug("User [" + user.getName() + "] setting to active.");
                    user.updateToken(iguassuToken);
                    UserDBManager.getInstance().update(user);
                }
            } else {
                iguassuToken = this.generateIguassuToken(oAuthToken.getUserId());
                User newUser = new User(oAuthToken.getUserId(), iguassuToken);
                UserDBManager.getInstance().save(newUser);
                logger.info("OAuth2 tokens for the user [" + oAuthToken.getUserId() + "] was stored.");
            }
            this.storeNewToken(oAuthToken);
            return user;

        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public User authorize(Credential credentials) throws GeneralSecurityException {
        User user;
        try {
            user = Objects.requireNonNull(UserDBManager.getInstance().findUserByName((credentials.getUserId())));
        } catch (NullPointerException npe) {
            throw new GeneralSecurityException("The user couldn't be retrieved.");
        }

        logger.debug("Authorizing user with identifier: [" + user.getName() + "]");

        if (!user.getIguassuToken().equalsIgnoreCase(credentials.getIguassuToken())) {
            throw new GeneralSecurityException(
                    "User " + user.getName() + " not has a valid Iguassu token.");
        }

        return user;
    }

    @Override
    public OAuthToken refreshOAuth2Token(OAuthToken oAuthToken) throws GeneralSecurityException {
        return this.requestsHelper.refreshToken(oAuthToken);
    }

    private void storeNewToken(OAuthToken oAuthToken) {
        OAuthToken lastToken = OAuthTokenDBManager.getInstance().findByUserId(oAuthToken.getUserId());

        if (Objects.nonNull(lastToken)) {
            oAuthToken.setVersion(lastToken.getVersion() + 1);
            OAuthTokenDBManager.getInstance().delete(lastToken.getId());
        }
        OAuthTokenDBManager.getInstance().save(oAuthToken);
    }

    private String generateIguassuToken(String userId) {
        final String sessionToken =
                new RandomString(DEFAULT_IGUASSU_TOKEN_LENGTH, userId).nextString();

        return Base64.getEncoder().encodeToString(sessionToken.getBytes());
    }
}

package org.fogbowcloud.app.core.auth;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.exceptions.UserNotExistException;
import org.fogbowcloud.app.core.models.user.*;
import org.fogbowcloud.app.core.datastore.managers.UserDBManager;
import org.fogbowcloud.app.utils.RandomString;

import javax.transaction.Transactional;
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
    private final UserDBManager userDBManager = UserDBManager.getInstance();

    private AuthRequestsHelper requestsHelper;

    public DefaultAuthManager(Properties properties) {
        this.requestsHelper = new AuthRequestsHelper(properties);
    }

    @Override
    @Transactional
    public User authenticate(OAuth2Identifiers oAuth2Identifiers, String authorizationCode, int nonce) {
        try {
            OAuthToken oAuthToken =
                    this.requestsHelper.getToken(oAuth2Identifiers, authorizationCode);
            User user = this.userDBManager.findUserByAlias(oAuthToken.getUserId());

            if (Objects.nonNull(user)) {
                logger.info("The user " + user.getAlias() + " has been found in our database.");
                if (user.isActive()) {
                    logger.info("User " + user.getAlias() + " is active and has already valid credentials.");
                } else {
                    oAuthToken.setVersion(user.getCredentials().getOauthToken().getVersion() + 1);
                    Credential newCredentials = updateUserCredentials(oAuthToken, user, nonce);
                    user.changeSessionState(SessionState.ACTIVE);
                    user.setCredentials(newCredentials);
                    logger.info("Updating user " + user.getAlias() + " to active.");
                    this.userDBManager.update(user);
                }
            } else {
                logger.info("User " + oAuthToken.getUserId() + " does not exist yet, but has authorization and will" +
                        " be stored for future orders");
                final String iguassuToken = this.generateIguassuToken(oAuthToken.getUserId());
                final Credential credentials = new Credential(iguassuToken, nonce, oAuthToken);
                user = new User(oAuthToken.getUserId(), credentials);
                this.userDBManager.save(user);
                logger.info("User " + oAuthToken.getUserId() + " has been successfully authenticated.");
            }

            return user;

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    public User authorize(RequesterCredential requesterCredentials) throws GeneralSecurityException, UserNotExistException {
        User user;
        try {
            user = Objects.requireNonNull(this.userDBManager.findOne(requesterCredentials.getUserId()));
        } catch (NullPointerException npe) {
            throw new UserNotExistException("The user could not be recovered because it has not yet been stored.");
        }

        logger.info("Authorizing user with identifier: [" + user.getAlias() + "]");

        if (!user.getCredentials().getIguassuToken().equalsIgnoreCase(requesterCredentials.getIguassuToken())) {
            throw new GeneralSecurityException("User " + user.getAlias() + " not has a valid Iguassu token.");
        }

        return user;
    }

    @Override
    public OAuthToken refreshOAuth2Token(OAuthToken oAuthToken) throws GeneralSecurityException {
        return this.requestsHelper.refreshToken(oAuthToken);
    }

    private Credential updateUserCredentials(OAuthToken newOAuthToken, User user, int nonce) {
        logger.debug("Generating new credentials for user " + user.getAlias() + ".");
        final OAuthToken lastToken = user.getCredentials().getOauthToken();
        final String iguassuToken = this.generateIguassuToken(user.getAlias());
        if (Objects.nonNull(lastToken)) {
            newOAuthToken.setVersion(lastToken.getVersion() + 1);
        }

        return new Credential(iguassuToken, nonce, newOAuthToken);
    }

    private String generateIguassuToken(String userId) {
        final String sessionToken =
                new RandomString(DEFAULT_IGUASSU_TOKEN_LENGTH, userId).nextString();

        return Base64.getEncoder().encodeToString(sessionToken.getBytes());
    }
}

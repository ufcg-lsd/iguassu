package org.fogbowcloud.app.core.auth;

import java.security.GeneralSecurityException;
import org.fogbowcloud.app.core.auth.models.Credential;
import org.fogbowcloud.app.core.auth.models.OAuth2Identifiers;
import org.fogbowcloud.app.core.auth.models.OAuthToken;
import org.fogbowcloud.app.core.auth.models.User;

/**
 * This interface defines the operations that an authentication and authorization manager must have.
 */
public interface AuthManager {
    /**
     * Registers a user if there's no already exist or update it credentials case already exists.
     *
     * @return a user with credentials updated.
     * @param oAuth2Identifiers is the information provided by the OAuth2 protocol.
     * @param authorizationCode is the code that allows requesting an authentication.
     */
    User authenticate(OAuth2Identifiers oAuth2Identifiers, String authorizationCode)
            throws GeneralSecurityException;

    /**
     * Refreshes information for a given user {@link OAuthToken} using the {@link
     * OAuthToken#getRefreshToken()} generating new tokens without a new authentication.
     *
     * @param oAuthToken the users token that will be refreshed.
     * @return an {@link OAuthToken} with it tokens refreshed.
     * @throws Exception if the connection with the Storage Service is down.
     */
    OAuthToken refreshOAuth2Token(OAuthToken oAuthToken) throws Exception;

    /**
     * Retrieves an authorized user. This operation verifies if the user already has some registry
     * of authentication and if he has an updated and the valid of it credentials.
     *
     * @param credentials of the user.
     * @return an authorized user instance.
     * @throws GeneralSecurityException if any information has a wrong shape or is expired.
     */
    User authorize(Credential credentials) throws GeneralSecurityException;

    /**
     * Stores a new user on the database.
     *
     * @param userId is the identifier of the user. Must be unique.
     * @param token is the public token used to clients communicate with the Iguassu.
     * @return an authorized user instance; May be null depending on the if the
     */
    User store(String userId, String token);

    /**
     * Returns the user if they have already authenticated before. May be null depending on the if
     * the {@link User doesn't exist}.
     */
    User retrieve(String userId);

    /** Replaces the state by the user state received in params. */
    void update(User currentUser);
}
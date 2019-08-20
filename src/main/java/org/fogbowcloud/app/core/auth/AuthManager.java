package org.fogbowcloud.app.core.auth;

import java.security.GeneralSecurityException;

import org.fogbowcloud.app.core.exceptions.UserNotExistException;
import org.fogbowcloud.app.core.models.user.*;

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
     * @param nonce a nonce code represents the hash of the request.
     */
    User authenticate(OAuth2Identifiers oAuth2Identifiers, String authorizationCode, int nonce)
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
     * @param requesterCredential the credentials of the user.
     * @return an authorized user instance.
     * @throws GeneralSecurityException if any information has a wrong shape or is expired.
     */
    User authorize(RequesterCredential requesterCredential) throws GeneralSecurityException, UserNotExistException;
}

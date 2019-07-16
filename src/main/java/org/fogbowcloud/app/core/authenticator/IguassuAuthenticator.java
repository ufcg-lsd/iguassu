package org.fogbowcloud.app.core.authenticator;

import java.security.GeneralSecurityException;
import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.models.User;

public interface IguassuAuthenticator {

    User authorizesUser(Credential credential) throws GeneralSecurityException;

    User addUser(String userId, String secretKey);

    User getUserByUsername(String userId);
}

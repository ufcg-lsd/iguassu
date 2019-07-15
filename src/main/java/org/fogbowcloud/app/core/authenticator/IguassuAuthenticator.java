package org.fogbowcloud.app.core.authenticator;

import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.models.User;

public interface IguassuAuthenticator {

    User authenticateUser(Credential credential);

    User addUser(String userId, String secretKey);

    User getUserByUsername(String userId);
}

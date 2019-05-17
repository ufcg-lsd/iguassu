package org.fogbowcloud.app.core.authenticator;

import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.models.User;

public interface IguassuAuthenticator {
	User authenticateUser(Credential credential);
	User addUser(String username, String password);
	User getUserByUsername(String username);
}

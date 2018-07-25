package org.fogbowcloud.app.utils.authenticator;

import org.fogbowcloud.app.model.User;

public interface IguassuAuthenticator {
	User authenticateUser(Credential credential);
	User addUser(String username, String password);
	User getUserByUsername(String username);
	String getAuthenticatorName();
}

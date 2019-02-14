package org.fogbowcloud.app.integration;

import java.util.Properties;

import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.utils.authenticator.IguassuAuthenticator;
import org.fogbowcloud.app.utils.authenticator.Credential;

public class FakeAuthenticationPlugin implements IguassuAuthenticator {

	boolean authenticate;
	
	public FakeAuthenticationPlugin(Properties properties) {
		this.authenticate = Boolean.parseBoolean(properties.getProperty("authenticate_user"));
	}
	
	@Override
	public User authenticateUser(Credential credential) {
		return new FakeUser(credential.getUsername(), "iguassuService");
	}

	@Override
	public User addUser(String username, String password) {		
		return new FakeUser(username, "iguassuService");
	}

	@Override
	public User getUserByUsername(String username) {
		return new FakeUser(username, "iguassuService");
	}

	@Override
	public String getAuthenticatorName() {
		return "fake_authenticator";
	}

}

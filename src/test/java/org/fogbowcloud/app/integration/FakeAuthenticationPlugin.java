package org.fogbowcloud.app.integration;

import java.util.Properties;

import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.utils.authenticator.ArrebolAuthenticator;
import org.fogbowcloud.app.utils.authenticator.Credential;

public class FakeAuthenticationPlugin implements ArrebolAuthenticator {

	boolean authenticate;
	
	public FakeAuthenticationPlugin(Properties properties) {
		this.authenticate = Boolean.parseBoolean(properties.getProperty("authenticate_user"));
	}
	
	@Override
	public User authenticateUser(Credential credential) {
		return new FakeUser(credential.getUsername(), "arrebolservice");
	}

	@Override
	public User addUser(String username, String password) {		
		return new FakeUser(username, "arrebolservice");
	}

	@Override
	public User getUserByUsername(String username) {
		// TODO Auto-generated method stub
		return new FakeUser(username, "arrebolservice");
	}

	@Override
	public String getAuthenticatorName() {
		// TODO Auto-generated method stub
		return "fake_authenticator";
	}

}

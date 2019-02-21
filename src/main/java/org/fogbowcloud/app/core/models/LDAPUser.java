package org.fogbowcloud.app.core.models;

public class LDAPUser implements User{

	private String user;
	private String username;
	
	public LDAPUser(String user, String username) {
		this.user = user;
		this.username = username;
	}
	
	@Override
	public String getUser() {
		return this.user;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

}

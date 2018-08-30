package org.fogbowcloud.app.integration;

import org.fogbowcloud.app.model.User;

public class FakeUser implements User {

	public String username;
	public String uuid;
	
	public FakeUser(String username, String uuid) {
		this.username = username;
		this.uuid = uuid;
	}
	
	@Override
	public String getUser() {
		return this.username;
	}

	@Override
	public String getUsername() {
		return this.uuid;
	}

}

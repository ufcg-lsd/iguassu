package org.fogbowcloud.app.model;

import org.json.JSONException;
import org.json.JSONObject;

public class UserImpl implements User {

	private static final String DEFAULT_USER = "1111";
	private String username;
	private String publicKey;
	private boolean active;

	public UserImpl(String username, String publicKey) {
		this.username = username;
		this.publicKey = publicKey;
		this.setActive(true);
	}

	@Override
	public String getUser() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject user = new JSONObject();
		user.put("username", this.username);
		user.put("publicKey", this.publicKey);
		return user;
	}

	public static UserImpl fromJSON(JSONObject userJSON) {
		return new UserImpl(userJSON.optString("username"),
				userJSON.optString("publicKey"));
	}

	@Override
	public String getUsername() {
		return  (this.username != null) ? this.username : DEFAULT_USER;
	}
}

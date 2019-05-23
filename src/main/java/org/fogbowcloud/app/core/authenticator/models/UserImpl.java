package org.fogbowcloud.app.core.authenticator.models;

import org.json.JSONException;
import org.json.JSONObject;

public class UserImpl implements User {

	private String userId;
	private String iguassuToken;
	private boolean active;

	public UserImpl(String userId, String iguassuToken) {
		this.userId = userId;
		this.iguassuToken = iguassuToken;
		this.setActive(true);
	}

	@Override
	public String getUserIdentification() {
		return userId;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject user = new JSONObject();
		user.put("userId", this.userId);
		user.put("iguassuToken", this.iguassuToken);
		user.put("active", this.active);
		return user;
	}

	public static User fromJSON(JSONObject userJSON) {
		return new UserImpl(userJSON.optString("userId"),
				userJSON.optString("iguassuToken"));
	}
}

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
		return this.userId;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public String getIguassuToken() {
		return this.iguassuToken;
	}

	@Override
	public void setActive(boolean isActive) {
		this.active = isActive;
	}

	@Override
	public void updateIguassuToken(String iguassuToken) {
		this.iguassuToken = iguassuToken;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject user = new JSONObject();
		user.put("userId", this.userId);
		user.put("iguassuToken", this.iguassuToken);
		user.put("active", this.active);
		return user;
	}

	public static User fromJSON(JSONObject userJSON) {
		UserImpl user = new UserImpl(userJSON.optString("userId"),
			userJSON.optString("iguassuToken"));
		user.setActive(userJSON.optBoolean("active"));
		return user;
	}
}

package org.fogbowcloud.app.core.authenticator.models;

import java.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;

public class UserImpl implements User {

	private static final String USER_ID_JSON_KEY = "userId";
	private static final String IGUASSU_TOKEN_JSON_KEY = "iguassuToken";
	private static final String ACTIVE_JSON_KEY = "active";
	private static final String SESSION_TIME_JSON_KEY = "sessionTime";

	private String userId;
	private String iguassuToken;
	private boolean active;
	private long sessionTime;

	public UserImpl(String userId, String iguassuToken) {
		this.userId = userId;
		this.iguassuToken = iguassuToken;
		this.resetSession();
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
	public long getSessionTime() {
		return this.sessionTime;
	}

	@Override
	public void resetSession() {
		this.sessionTime = Instant.now().getEpochSecond();
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
		user.put(USER_ID_JSON_KEY, this.userId);
		user.put(IGUASSU_TOKEN_JSON_KEY, this.iguassuToken);
		user.put(ACTIVE_JSON_KEY, this.active);
		user.put(SESSION_TIME_JSON_KEY, this.sessionTime);
		return user;
	}

	public static User fromJSON(JSONObject userJSON) {
		UserImpl user = new UserImpl(userJSON.optString(USER_ID_JSON_KEY),
			userJSON.optString(IGUASSU_TOKEN_JSON_KEY));
		user.setActive(userJSON.optBoolean(ACTIVE_JSON_KEY));
		user.sessionTime = userJSON.optLong(SESSION_TIME_JSON_KEY);
		return user;
	}
}

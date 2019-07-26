package org.fogbowcloud.app.core.auth.models;

import java.time.Instant;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultUser implements User {

	private String userId;
	private String iguassuToken;
	private SessionState sessionState;
	private long sessionTime;

	public DefaultUser(String userId, String iguassuToken) {
		this.userId = userId;
		this.iguassuToken = iguassuToken;
		this.resetSession();
		this.sessionState = SessionState.ACTIVE;
	}

	public static JSONObject toJSON(User user) throws JSONException {
		JSONObject userJson = new JSONObject();
		userJson.put(UserJsonKey.USER_ID.getJsonKey(), user.getIdentifier());
		userJson.put(UserJsonKey.IGUASSU_TOKEN.getJsonKey(), user.retrieveToken());
		userJson.put(UserJsonKey.SESSION_STATE.getJsonKey(), user.isActive());
		userJson.put(UserJsonKey.SESSION_TIME.getJsonKey(), user.getSessionTime());
		return userJson;
	}

	public static User fromJSON(JSONObject userJSON) {
		DefaultUser user =
			new DefaultUser(
				userJSON.optString(UserJsonKey.USER_ID.getJsonKey()),
				userJSON.optString(UserJsonKey.IGUASSU_TOKEN.getJsonKey())
			);

		final String sessionState = userJSON.optString(UserJsonKey.IGUASSU_TOKEN.getJsonKey());

		user.changeSessionState(SessionState.valueOf(sessionState));
		user.setSessionTime(userJSON.optLong(UserJsonKey.SESSION_TIME.getJsonKey()));
		return user;
	}

	@Override
	public String getIdentifier() {
		return this.userId;
	}

	@Override
	public boolean isActive() {
		return this.sessionState == SessionState.ACTIVE;
	}

	@Override
	public void changeSessionState(SessionState state) {
		this.sessionState = state;
	}

	@Override
	public String retrieveToken() {
		return this.iguassuToken;
	}

	@Override
	public long getSessionTime() {
		return this.sessionTime;
	}

	public void setSessionTime(long sessionTime) {
		this.sessionTime = sessionTime;
	}

	@Override
	public void resetSession() {
		this.sessionTime = Instant.now().getEpochSecond();
		this.sessionState = SessionState.ACTIVE;
	}

	@Override
	public void updateToken(String token) {
		this.iguassuToken = token;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject user = new JSONObject();
		user.put(UserJsonKey.USER_ID.getJsonKey(), this.userId);
		user.put(UserJsonKey.IGUASSU_TOKEN.getJsonKey(), this.iguassuToken);
		user.put(UserJsonKey.SESSION_STATE.getJsonKey(), this.sessionState.getState());
		user.put(UserJsonKey.SESSION_TIME.getJsonKey(), this.sessionTime);
		return user;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DefaultUser user = (DefaultUser) o;
		return sessionState == user.sessionState
			&& sessionTime == user.sessionTime
			&& userId.equals(user.userId)
			&& iguassuToken.equals(user.iguassuToken);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, iguassuToken, sessionState, sessionTime);
	}
}

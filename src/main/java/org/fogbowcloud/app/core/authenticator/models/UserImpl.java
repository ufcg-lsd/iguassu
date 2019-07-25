package org.fogbowcloud.app.core.authenticator.models;

import java.time.Instant;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class UserImpl implements User {

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
    public String getIdentifier() {
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

    public void setSessionTime(long sessionTime) {
        this.sessionTime = sessionTime;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject user = new JSONObject();
        user.put(UserJsonKey.USER_ID.getJsonKey(), this.userId);
        user.put(UserJsonKey.IGUASSU_TOKEN.getJsonKey(), this.iguassuToken);
        user.put(UserJsonKey.ACTIVE.getJsonKey(), this.active);
        user.put(UserJsonKey.SESSION_TIME.getJsonKey(), this.sessionTime);
        return user;
    }

    public static JSONObject toJSON(User user) throws JSONException {
        JSONObject userJson = new JSONObject();
        userJson.put(UserJsonKey.USER_ID.getJsonKey(), user.getIdentifier());
        userJson.put(UserJsonKey.IGUASSU_TOKEN.getJsonKey(), user.getIguassuToken());
        userJson.put(UserJsonKey.ACTIVE.getJsonKey(), user.isActive());
        userJson.put(UserJsonKey.SESSION_TIME.getJsonKey(), user.getSessionTime());
        return userJson;
    }

    public static User fromJSON(JSONObject userJSON) {
        UserImpl user = new UserImpl(userJSON.optString(UserJsonKey.USER_ID.getJsonKey()),
                userJSON.optString(UserJsonKey.IGUASSU_TOKEN.getJsonKey()));
        user.setActive(userJSON.optBoolean(UserJsonKey.ACTIVE.getJsonKey()));
        user.setSessionTime(userJSON.optLong(UserJsonKey.SESSION_TIME.getJsonKey()));
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
        UserImpl user = (UserImpl) o;
        return active == user.active &&
                sessionTime == user.sessionTime &&
                userId.equals(user.userId) &&
                iguassuToken.equals(user.iguassuToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, iguassuToken, active, sessionTime);
    }
}

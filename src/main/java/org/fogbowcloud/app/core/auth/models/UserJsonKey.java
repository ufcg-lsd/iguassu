package org.fogbowcloud.app.core.auth.models;

public enum UserJsonKey {

    USER_ID("userId"),
    IGUASSU_TOKEN("iguassuToken"),
    SESSION_STATE("sessionState"),
    SESSION_TIME("sessionTime");

    private final String jsonKey;

    UserJsonKey(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getJsonKey() {
        return this.jsonKey;
    }

}

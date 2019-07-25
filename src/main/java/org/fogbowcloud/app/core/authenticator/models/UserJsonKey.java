package org.fogbowcloud.app.core.authenticator.models;

public enum UserJsonKey {

    USER_ID("userId"),
    IGUASSU_TOKEN("iguassuToken"),
    ACTIVE("active"),
    SESSION_TIME("sessionTime");

    private final String jsonKey;

    UserJsonKey(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getJsonKey() {
        return this.jsonKey;
    }

}

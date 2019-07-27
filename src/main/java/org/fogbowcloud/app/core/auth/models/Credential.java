package org.fogbowcloud.app.core.auth.models;

import org.fogbowcloud.app.core.constants.JsonKey;
import org.json.JSONException;
import org.json.JSONObject;

/** This class encapsulates the user security information. */
public class Credential {

    private String userId;
    private String iguassuToken;
    private Integer nonce;

    public Credential(String userId, String iguassuToken, Integer nonce) {
        this.userId = userId;
        this.iguassuToken = iguassuToken;
        this.nonce = nonce;
    }

    public static Credential fromJSON(JSONObject credential) {
        return new Credential(
                credential.optString(JsonKey.USER_ID.getKey()),
                credential.optString(JsonKey.IGUASSU_TOKEN.getKey()),
                credential.optInt(JsonKey.NONCE.getKey()));
    }

    public JSONObject toJSON() {
        JSONObject credential = new JSONObject();
        try {
            credential.put(JsonKey.USER_ID.getKey(), this.userId);
            credential.put(JsonKey.IGUASSU_TOKEN.getKey(), this.iguassuToken);
            credential.put(JsonKey.NONCE.getKey(), this.nonce);
        } catch (JSONException e) {
            return null;
        }
        return credential;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIguassuToken() {
        return iguassuToken;
    }

    public void setIguassuToken(String iguassuToken) {
        this.iguassuToken = iguassuToken;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }
}

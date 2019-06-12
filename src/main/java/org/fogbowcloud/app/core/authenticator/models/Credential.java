package org.fogbowcloud.app.core.authenticator.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Credential {

	private static final String USER_ID_JSON_KEY = "userId";
	private static final String IGUASSU_TOKEN_JSON_KEY = "iguassuToken";
	private static final String NONCE_KEY = "nonce";

	private String userId;
	private String iguassuToken;
	private Integer nonce;
	
	public Credential(String userId, String iguassuToken, Integer nonce) {
		this.userId = userId;
		this.iguassuToken = iguassuToken;
		this.nonce = nonce;
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
	
	public static Credential fromJSON(JSONObject credential) {
		return new Credential(
				credential.optString(USER_ID_JSON_KEY),
				credential.optString(IGUASSU_TOKEN_JSON_KEY),
				credential.optInt(NONCE_KEY));
	}
	
	public JSONObject toJSON() {
		JSONObject credential = new JSONObject();
		try {
			credential.put(USER_ID_JSON_KEY, this.userId);
			credential.put(IGUASSU_TOKEN_JSON_KEY, this.iguassuToken);
			credential.put(NONCE_KEY, this.nonce);
		} catch (JSONException e) {
			return null;
		}
		return credential;
	}

}

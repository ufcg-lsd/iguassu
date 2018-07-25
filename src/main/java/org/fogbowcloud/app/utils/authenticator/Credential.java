package org.fogbowcloud.app.utils.authenticator;

import org.json.JSONException;
import org.json.JSONObject;

public class Credential {

	private static final String USER_NAME_KEY = "username";
	private static final String USER_TOKEN_KEY = "token";
	private static final String NONCE_KEY = "nonce";

	private String username;
	private String token;
	private Integer nonce;
	
	public Credential(String username, String token, Integer nonce) {
		this.username = username;
		this.token = token;
		this.nonce = nonce;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getNonce() {
		return nonce;
	}

	public void setNonce(Integer nonce) {
		this.nonce = nonce;
	}
	
	public static Credential fromJSON(JSONObject credential) {
		return new Credential(
				credential.optString(USER_NAME_KEY),
				credential.optString(USER_TOKEN_KEY),
				credential.optInt(NONCE_KEY));
	}
	
	public JSONObject toJSON() {
		JSONObject credential = new JSONObject();
		try {
			credential.put(USER_NAME_KEY, this.username);
			credential.put(	USER_TOKEN_KEY, this.token);
			credential.put(NONCE_KEY, this.nonce);
		} catch (JSONException e) {
			return null;
		}
		return credential;
	}

}

package org.fogbowcloud.app.utils.authenticator;

import org.json.JSONException;
import org.json.JSONObject;

public class Credential {
	
	private String username;
	private String password;
	private Integer nonce;
	
	public Credential(String username, String password, Integer nonce) {
		this.username = username;
		this.password = password;
		this.nonce = nonce;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getNonce() {
		return nonce;
	}

	public void setNonce(Integer nonce) {
		this.nonce = nonce;
	}
	
	public static Credential fromJSON(JSONObject credential) {
		return new Credential(
				credential.optString("username"), 
				credential.optString("password"), 
				credential.optInt("nonce"));
	}
	
	public JSONObject toJSON() {
		JSONObject credential = new JSONObject();
		try {
			credential.put("username", this.username);
			credential.put("password", this.password);
			credential.put("nonce", this.nonce);
		} catch (JSONException e) {
			return null;
		}
		return credential;
	}

}

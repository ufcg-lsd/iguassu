package org.fogbowcloud.app.core.auth.models;

public enum SessionState {
	ACTIVE("active"),
	EXPIRED("expired");

	private String state;

	SessionState(String state) {
		this.state = state;
	}

	public String getState() {
		return this.state;
	}
}

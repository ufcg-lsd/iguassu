package org.fogbowcloud.app.core.authenticator.models;

public interface User {

	String getIdentifier();

	boolean isActive();

	void setActive(boolean isActive);

	void updateIguassuToken(String iguassuToken);

	String getIguassuToken();

	long getSessionTime();

	void resetSession();
}

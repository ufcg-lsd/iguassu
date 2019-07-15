package org.fogbowcloud.app.core.authenticator.models;

public interface User {

	String getUserIdentification();

	boolean isActive();

	void setActive(boolean isActive);

	void updateIguassuToken(String iguassuToken);

	String getIguassuToken();
}

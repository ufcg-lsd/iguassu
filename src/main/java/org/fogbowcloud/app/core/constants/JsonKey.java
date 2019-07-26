package org.fogbowcloud.app.core.constants;

public enum JsonKey {
	USER_ID("user_id"),

	IGUASSU_TOKEN("iguassu_token"),

	SESSION_STATE("session_state"),

	SESSION_TIME("session_time"),

	COMMAND("command"),

	RAW_COMMAND("raw_command"),

	STATE("state"),

	EXIT_CODE("exit_code"),

	REQUIREMENTS("requirements"),

	DOCKER_IMAGE("docker_image");

	private final String key;

	JsonKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
}

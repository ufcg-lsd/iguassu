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
	DOCKER_IMAGE("docker_image"),
	JOB_ID("job_id"),
	EXECUTION_ID("execution_id"),
	LABEL("label"),
	TASKS("tasks"),
	TIMESTAMP("timestamp"),
	NONCE("nonce"),
	TOKEN_VERSION("version"),
	AUTHORIZATION_CODE("authorization_code");

	private final String key;

	JsonKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
}

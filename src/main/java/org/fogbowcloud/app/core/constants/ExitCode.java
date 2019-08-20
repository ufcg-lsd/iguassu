package org.fogbowcloud.app.core.constants;

public enum ExitCode {
	FAIL(1);

	private final int code;

	ExitCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}

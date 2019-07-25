package org.fogbowcloud.app.core.command;

public enum CommandJsonKey {

    COMMAND("command"),
    STATE("state"),
    RAW_COMMAND("raw_command"),
    EXIT_CODE("exit_code");

    private final String jsonKey;

    CommandJsonKey(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getJsonKey() {
        return this.jsonKey;
    }
}

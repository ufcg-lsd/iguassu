package org.fogbowcloud.app.core.command;

public enum CommandJsonKey {

    JSON_KEY_COMMAND("command"),
    JSON_KEY_STATE("state"),
    JSON_KEY_RAW_COMMAND("raw_command"),
    JSON_KEY_EXIT_CODE("exit_code");

    private final String jsonkey;

    CommandJsonKey(String jsonkey) {
        this.jsonkey = jsonkey;
    }

    public String getJsonkey() {
        return this.jsonkey;
    }
}

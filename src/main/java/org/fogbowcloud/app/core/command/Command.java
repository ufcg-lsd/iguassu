package org.fogbowcloud.app.core.command;

import java.io.Serializable;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Command implements Serializable {

    private static final long serialVersionUID = 5281647552435522413L;
    private static final Logger logger = Logger.getLogger(Command.class);
    private static final int UNDETERMINED_RESULT = Integer.MAX_VALUE;

    private final String command;
    private final String rawCommand;
    private CommandState state;
    private int exitCode;

    public Command(String command, CommandState state) {
        this.rawCommand = command;
        this.command = command;
        this.state = state;
        this.exitCode = UNDETERMINED_RESULT;
    }

    public Command(String command) {
        this(command, CommandState.QUEUED);
    }

    public Command(String command, String rawCommand) {
        this.command = command;
        this.rawCommand = rawCommand;
        this.state = CommandState.QUEUED;
        this.exitCode = UNDETERMINED_RESULT;
    }

    public String getCommand() {
        return command;
    }

    public CommandState getState() {
        return this.state;
    }

    public void setState(CommandState state) {
        this.state = state;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getRawCommand() {
        return this.rawCommand;
    }

    public Command clone() {
        return new Command(this.command, this.state);
    }

    public JSONObject toJSON() {
        try {
            JSONObject command = new JSONObject();
            command.put(CommandJsonKey.COMMAND.getJsonKey(), this.getCommand());
            command.put(CommandJsonKey.STATE.getJsonKey(), this.getState().toString());
            command.put(CommandJsonKey.RAW_COMMAND.getJsonKey(), this.getRawCommand());
            command.put(CommandJsonKey.EXIT_CODE.getJsonKey(), this.getExitCode());
            return command;
        } catch (JSONException e) {
            logger.debug("Error while trying to create a JSON from command", e);
            return null;
        }
    }

    public static Command fromJSON(JSONObject commandJSON) {
        Command command = new Command(
                commandJSON.optString(CommandJsonKey.COMMAND.getJsonKey()),
                commandJSON.optString(CommandJsonKey.RAW_COMMAND.getJsonKey())
        );
        command.setState(CommandState.valueOf(commandJSON.optString(CommandJsonKey.STATE.getJsonKey())));
        command.setExitCode(commandJSON.getInt(CommandJsonKey.EXIT_CODE.getJsonKey()));
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Command command1 = (Command) o;
        return command.equals(command1.command) &&
                state == command1.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, state);
    }
}

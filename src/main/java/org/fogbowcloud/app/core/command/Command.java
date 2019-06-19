package org.fogbowcloud.app.core.command;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class Command implements Serializable {
    private static final long serialVersionUID = 5281647552435522413L;
    private static final Logger LOGGER = Logger.getLogger(Command.class);
    private static final int UNDETERMINED_RESULT = Integer.MAX_VALUE; 

    private final String command;
    private CommandState state;
    private int exitCode;

    public Command(String command, CommandState state) {
        this.command = command;
        this.state = state;
        this.exitCode = UNDETERMINED_RESULT;
    }

    public Command(String command) {
        this(command, CommandState.QUEUED);
    }

    public String getCommand() {
        return command;
    }

    public void setState(CommandState state) {
        this.state = state;
    }

    public CommandState getState() {
        return this.state;
    }
    
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    public Command clone() {
        return new Command(this.command, this.state);
    }

    public JSONObject toJSON() {
        try {
            JSONObject command = new JSONObject();
            command.put("command", this.getCommand());
            command.put("state", this.getState().toString());
            return command;
        } catch (JSONException e) {
            LOGGER.debug("Error while trying to create a JSON from command", e);
            return null;
        }
    }

    public static Command fromJSON(JSONObject commandJSON) {
        Command command = new Command(commandJSON.optString("command"));
        command.setState(CommandState.valueOf(commandJSON.optString("state")));
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command1 = (Command) o;
        return command.equals(command1.command) &&
                state == command1.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, state);
    }
}

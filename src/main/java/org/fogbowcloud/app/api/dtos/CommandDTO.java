package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.fogbowcloud.app.core.models.command.Command;
import org.fogbowcloud.app.core.models.command.CommandState;

import java.io.Serializable;

/** Data transfer object projection */
public class CommandDTO implements Serializable {

    @JsonProperty("raw_command")
    private String rawCommand;

    private CommandState state;

    @JsonProperty("exit_code")
    private int exitCode;

    CommandDTO(Command command){
        this.rawCommand = command.getRawCommand();
        this.state = command.getState();
        this.exitCode = command.getExitCode();
    }

    public String getRawCommand() {
        return rawCommand;
    }

    public CommandState getState() {
        return state;
    }

    public int getExitCode() {
        return exitCode;
    }
}

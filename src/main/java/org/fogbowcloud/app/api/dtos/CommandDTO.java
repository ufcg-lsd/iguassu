package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.fogbowcloud.app.core.models.command.Command;
import org.fogbowcloud.app.core.models.command.CommandState;

import java.io.Serializable;

/** Data transfer object projection */
public class CommandDTO implements Serializable {

    @ApiModelProperty(notes = "The raw command", position = 1, example = "echo 'Hello World'")
    @JsonProperty("raw_command")
    private String rawCommand;

    @ApiModelProperty(notes = "The command state", position = 2, example = "FINISHED")
    private CommandState state;

    @ApiModelProperty(notes = "The exit code of command execution", position = 3, example = "0")
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

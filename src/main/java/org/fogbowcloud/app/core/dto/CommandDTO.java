package org.fogbowcloud.app.core.dto;

import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.command.CommandState;

public class CommandDTO {

    private String rawCommand;
    private CommandState state;
    private int exitCode;

    public CommandDTO(Command command){
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

package org.fogbowcloud.app.core.models.command;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A command is a unit of a task. Each line of a task is a command.
 */
@Entity
@Table(name = "command")
public class Command implements Serializable {

    private static final long serialVersionUID = 5281647552435522413L;

    private static final int UNDETERMINED_RESULT = Integer.MAX_VALUE;
    private static final String COMMAND_COLUMN_NAME = "command";
    private static final String RAW_COMMAND_COLUMN_NAME = "raw_command";
    private static final String STATE_COLUMN_NAME = "state_command";
    private static final String EXIT_CODE_COLUMN_NAME = "state_command";
    @Column(name = COMMAND_COLUMN_NAME)
    private final String command;
    @Column(name = RAW_COMMAND_COLUMN_NAME)
    private final String rawCommand;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = STATE_COLUMN_NAME)
    @Enumerated(EnumType.STRING)
    private CommandState state;

    @Column(name = EXIT_CODE_COLUMN_NAME)
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

    public long getId() {
        return id;
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
        return command.equals(command1.command) && state == command1.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, state);
    }
}

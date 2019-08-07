package org.fogbowcloud.app.core.models.task;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.command.Command;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A task is a unit of a Job. A job is composed of an unordered set and allowing repetition of tasks
 * (bag). A task is composed of a set of {@link Command}.
 */
@Entity
@Table(name = "task")
public class Task {

    private static final Logger logger = Logger.getLogger(Task.class);
    private static final String ID_COLUMN_NAME = "id";
    private static final String SPECIFICATION_COLUMN_NAME = "specification";
    private static final String STATE_COLUMN_NAME = "state";
    private static final String COMMANDS_COLUMN_NAME = "commands";
    private static final String METADATA_COLUMN_NAME = "metadata";

    @Id
    @GeneratedValue
    @Column(name = ID_COLUMN_NAME)
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @Column(name = SPECIFICATION_COLUMN_NAME)
    private Specification specification;

    @Enumerated(EnumType.STRING)
    @Column(name = STATE_COLUMN_NAME)
    private TaskState state;

    @Column(name = COMMANDS_COLUMN_NAME)
    private List<Command> commands;

    @Column(name = METADATA_COLUMN_NAME)
    private Map<String, String> metadata;

    public Task() {
    }

    public Task(Specification specification) {
        this.commands = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.specification = specification;
        this.state = TaskState.READY;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public List<String> getAllCommandsInStr() {
        List<String> commandsStr = new ArrayList<>();
        for (Command command : this.commands) {
            commandsStr.add(command.getCommand());
        }
        return commandsStr;
    }

    public void putMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public void addCommand(Command command) {
        this.commands.add(command);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((specification == null) ? 0 : specification.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Task other = (Task) obj;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        if (specification == null) {
            return other.specification == null;
        } else return specification.equals(other.specification);
    }
}

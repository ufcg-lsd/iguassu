package org.fogbowcloud.app.core.models.task;

import org.fogbowcloud.app.core.models.command.Command;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;

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

    private static final String METADATA_MAP_KEY_COLUMN_NAME = "metadata_key";
    private static final String REQUIREMENTS_MAP_KEY_COLUMN_NAME = "requirements_key";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TaskState state;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Command> commands;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = METADATA_MAP_KEY_COLUMN_NAME)
    private Map<String, String> metadata;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = REQUIREMENTS_MAP_KEY_COLUMN_NAME)
    private Map<String, String> requirements;

    public Task() {}

    public Task(Map<String, String> requirements) {
        this.requirements = requirements;
        this.commands = new ArrayList<>();
        this.metadata = new HashMap<>();
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Map<String, String> getRequirements() {
        return requirements;
    }

    public void setRequirements(Map<String, String> requirements) {
        this.requirements = requirements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

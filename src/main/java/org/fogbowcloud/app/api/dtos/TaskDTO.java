package org.fogbowcloud.app.api.dtos;

import org.fogbowcloud.app.core.models.command.Command;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.core.models.task.TaskState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Data transfer object projection */
public class TaskDTO implements Serializable {

    private Long id;

    private List<CommandDTO> commands;

    private Map<String, String> requirements;

    private TaskState state;

    private Map<String, String> metadata;

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.commands = toCommandDTOList(task.getCommands());
        this.requirements = task.getRequirements();
        this.state = task.getState();
        this.metadata = task.getMetadata();
    }

    private List<CommandDTO> toCommandDTOList(List<Command> commands) {
        List<CommandDTO> l = new ArrayList<>();
        for (Command c : commands) {
            l.add(new CommandDTO(c));
        }
        return l;
    }

    public Long getId() {
        return id;
    }

    public List<CommandDTO> getCommands() {
        return commands;
    }

    public TaskState getState() {
        return state;
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
}

package org.fogbowcloud.app.api.dtos;

import io.swagger.annotations.ApiModelProperty;
import org.fogbowcloud.app.core.models.command.Command;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.core.models.task.TaskState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Data transfer object projection */
public class TaskDTO implements Serializable {

    @ApiModelProperty(notes = "The task id", position = 1, example = "3930bcaf-6864-44ce-b78c-398cd1e428d4")
    private Long id;

    @ApiModelProperty(notes = "The Command List of Task", position = 2, dataType = "CommandDTO")
    private List<CommandDTO> commands;

    @ApiModelProperty(notes = "The requirements for the task", position = 3)
    private Map<String, String> requirements;

    @ApiModelProperty(notes = "The task state", position = 4, example = "Finished")
    private TaskState state;

    @ApiModelProperty(notes = "The metadata map of the task", position = 5)
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

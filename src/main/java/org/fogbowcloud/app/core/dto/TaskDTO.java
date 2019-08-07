package org.fogbowcloud.app.core.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.app.core.models.command.Command;
import org.fogbowcloud.app.core.models.task.Specification;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.core.models.task.TaskState;

public class TaskDTO {

    private String id;
    private List<CommandDTO> commands;
    private Specification specification;
    private TaskState state;
    private Map<String, String> metadata;

    public TaskDTO(Task task){
        this.id = task.getId();
        this.commands = toCommandDTOList(task.getCommands());
        this.specification = task.getSpecification();
        this.state = task.getState();
        this.metadata = task.getMetadata();
    }

    private List<CommandDTO> toCommandDTOList(List<Command> commands){
        List<CommandDTO> l = new ArrayList<>();
        for(Command c : commands){
            l.add(new CommandDTO(c));
        }
        return l;
    }

    public String getId() {
        return id;
    }

    public List<CommandDTO> getCommands() {
        return commands;
    }

    public Specification getSpecification() {
        return specification;
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
}

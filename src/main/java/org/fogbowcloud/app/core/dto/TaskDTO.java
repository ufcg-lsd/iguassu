package org.fogbowcloud.app.core.dto;

import java.util.ArrayList;
import java.util.List;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.task.Specification;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskState;

public class TaskDTO {

    private String id;
    private List<CommandDTO> commands;
    private Specification specification;
    private TaskState state;

    public TaskDTO(Task task){
        this.id = task.getId();
        this.commands = toCommandDTOList(task.getAllCommands());
        this.specification = task.getSpecification();
        this.state = task.getState();
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
}

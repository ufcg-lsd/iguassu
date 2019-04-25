package org.fogbowcloud.app.core.dto;

import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.task.Specification;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TaskSpecDTO implements Serializable {
    private Specification spec;
    private List<Command> commands;
    private Map<String, String> metadata;

    public TaskSpecDTO(Specification spec, List<Command> commands, Map<String, String> metadata){
        this.spec = spec;
        this.commands = commands;
        this.metadata = metadata;
    }

    public TaskSpecDTO() { }

    public List<Command> getCommands(){
        return this.commands;
    }

    public Specification getSpec(){
        return this.spec;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    @Override
    public String toString() {
        return "TaskSpec{" +
                "spec=" + spec +
                ", commands=" + commands +
                ", metadata=" + metadata +
                '}';
    }

}
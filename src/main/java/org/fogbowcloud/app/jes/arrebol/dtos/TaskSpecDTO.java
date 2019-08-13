package org.fogbowcloud.app.jes.arrebol.dtos;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TaskSpecDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Map<String, String> requirements;
    private List<String> commands;
    private Map<String, String> metadata;

    public TaskSpecDTO(Long id, Map<String, String> requirements, List<String> commands,
                       Map<String, String> metadata) {
        this.id = id;
        this.requirements = requirements;
        this.commands = commands;
        this.metadata = metadata;
    }

    public TaskSpecDTO() { }

    public List<String> getCommands() {
        return this.commands;
    }


    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "TaskSpecDTO [id=" + id + ", requirements=" + this.requirements.toString() + ", commands=" +
                commands + ", metadata=" + metadata + "]";
    }

}

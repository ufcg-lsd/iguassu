package org.fogbowcloud.app.core.models.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.command.Command;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A task is a unit of a Job. A job is composed of an unordered set and allowing repetition of tasks
 * (bag). A task is composed of a set of {@link Command}.
 */
@Entity
@Table(name = "task")
public class Task {

    private static final Logger logger = Logger.getLogger(Task.class);

    @Id @GeneratedValue private String id;

    private Specification specification;

    @Enumerated(EnumType.STRING)
    private TaskState state;

    private List<Command> commands;
    private Map<String, String> metadata;

    public Task(String id, Specification specification, String uuid) {
        this.commands = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.id = id;
        this.specification = specification;
        this.state = TaskState.READY;
        this.uuid = uuid;
    }


    public List<String> getAllCommandsInStr() {
        List<String> commandsStr = new ArrayList<>();
        for (Command command : this.commands) {
            commandsStr.add(command.getCommand());
        }
        return commandsStr;
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

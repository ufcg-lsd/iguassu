package org.fogbowcloud.app.core.models.task;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.command.Command;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/** Default implementation of a {@link org.fogbowcloud.app.core.models.task.Task}. */
public class TaskImpl implements Task {

    private static final Logger logger = Logger.getLogger(TaskImpl.class);

    private String id;
    private String uuid;
    private Specification specification;
    private TaskState state;
    private List<Command> commands;
    private Map<String, String> metadata;

    public TaskImpl(String id, Specification specification, String uuid) {
        this.commands = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.id = id;
        this.specification = specification;
        this.state = TaskState.READY;
        this.uuid = uuid;
    }

    public static Task fromJSON(JSONObject taskJSON) {
        Specification specification =
                Specification.fromJSON(taskJSON.optJSONObject("specification"));
        Task task =
                new TaskImpl(taskJSON.optString("id"), specification, taskJSON.optString("uuid"));
        String taskState = taskJSON.optString("state");
        task.setState(TaskState.getTaskStateFromDesc(taskState));

        JSONArray commands = taskJSON.optJSONArray("commands");
        for (int i = 0; i < commands.length(); i++) {
            task.addCommand(Command.fromJSON(commands.optJSONObject(i)));
        }

        JSONObject metadata = taskJSON.optJSONObject("metadata");
        Iterator<?> metadataKeys = metadata.keys();
        while (metadataKeys.hasNext()) {
            String key = (String) metadataKeys.next();
            task.putMetadata(key, metadata.optString(key));
        }
        return task;
    }

    public JSONObject toJSON() {
        try {
            JSONObject task = new JSONObject();
            task.put("id", this.getId());
            task.put("specification", this.getSpecification().toJSON());
            task.put("uuid", this.getUUID());
            task.put("state", this.state.getDescription());
            JSONArray commands = new JSONArray();
            for (Command command : this.getAllCommands()) {
                commands.put(command.toJSON());
            }
            task.put("commands", commands);
            JSONObject metadata = new JSONObject();
            for (Map.Entry<String, String> entry : this.getAllMetadata().entrySet()) {
                metadata.put(entry.getKey(), entry.getValue());
            }
            task.put("metadata", metadata);
            return task;
        } catch (JSONException e) {
            logger.debug("Error while trying to create a JSON from task", e);
            return null;
        }
    }

    @Override
    public void putMetadata(String attributeName, String value) {
        metadata.put(attributeName, value);
    }

    @Override
    public String getMetadata(String attributeName) {
        return metadata.get(attributeName);
    }

    @Override
    public Specification getSpecification() {
        return this.specification;
    }

    @Override
    public List<Command> getAllCommands() {
        return commands;
    }

    public List<String> getAllCommandsInStr() {
        List<String> commandsStr = new ArrayList<>();
        for (Command command : this.commands) {
            commandsStr.add(command.getCommand());
        }
        return commandsStr;
    }

    @Override
    public Map<String, String> getAllMetadata() {
        return metadata;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void addCommand(Command command) {
        commands.add(command);
    }

    @Override
    public int getNumberOfCommands() {
        return commands.size();
    }

    @Override
    public TaskState getState() {
        return this.state;
    }

    @Override
    public void setState(TaskState state) {
        this.state = state;
    }

    @Override
    public String getUUID() {
        return this.uuid;
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
        TaskImpl other = (TaskImpl) obj;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        if (specification == null) {
            return other.specification == null;
        } else return specification.equals(other.specification);
    }
}

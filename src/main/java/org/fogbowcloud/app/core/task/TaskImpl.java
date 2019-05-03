package org.fogbowcloud.app.core.task;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class TaskImpl implements Task {

    private static final Logger LOGGER = Logger.getLogger(TaskImpl.class);

    public static final String ENV_LOCAL_OUT_DIR = "";

    public static final String METADATA_REMOTE_OUTPUT_FOLDER = "remote_output_folder";
    public static final String METADATA_LOCAL_OUTPUT_FOLDER = "local_output_folder";
    public static final String METADATA_REMOTE_COMMAND_EXIT_PATH = "remote_command_exit_path";
    public static final String METADATA_RESOURCE_ID = "resource_id";
    public static final String METADATA_TASK_TIMEOUT = "task_timeout";
    public static final String METADATA_MAX_RESOURCE_CONN_RETRIES = "max_conn_retries";

    private String id;
    private String uuid;
    private Specification specification;
    private TaskState state;
    private List<Command> commands;
    private List<String> commandsStr;
    private Map<String, String> metadata;

    public TaskImpl(String id, Specification specification, String uuid) {
        this.commands = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.id = id;
        this.specification = specification;
        this.state = TaskState.READY;
        this.uuid = uuid;
        this.commandsStr = new ArrayList<>();
    }

    private void populateCommandStrList() {
        for (int i = 0; i < this.commands.size(); i++) {
            this.commandsStr.add(this.commands.get(i).getCommand());
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
        populateCommandStrList();
        return this.commandsStr;
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

    public JSONObject toJSON() {
        try {
            JSONObject task = new JSONObject();
            task.put("id", this.getId());
            task.put("specification", this.getSpecification().toJSON());
            task.put("uuid", this.getUUID());
            task.put("state", this.state.getDesc());
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
            LOGGER.debug("Error while trying to create a JSON from task", e);
            return null;
        }
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaskImpl other = (TaskImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (specification == null) {
            return other.specification == null;
        } else
            return specification.equals(other.specification);
    }
}

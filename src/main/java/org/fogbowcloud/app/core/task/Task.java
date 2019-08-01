package org.fogbowcloud.app.core.task;

import org.fogbowcloud.app.core.command.Command;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * A task is a unit of a Job. A job is composed of an unordered set and allowing repetition of tasks
 * (bag). A task is composed of a set of {@link Command}.
 */
public interface Task {

    Specification getSpecification();

    String getId();

    void addCommand(Command command);

    List<Command> getAllCommands();

    List<String> getAllCommandsInStr();

    void putMetadata(String attributeName, String value);

    String getMetadata(String attributeName);

    Map<String, String> getAllMetadata();

    int getNumberOfCommands();

    JSONObject toJSON();

    TaskState getState();

    void setState(TaskState state);

    String getUUID();
}

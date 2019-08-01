package org.fogbowcloud.app.jdfcompiler.job;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.JsonKey;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/** It add the job label and user information path to the {@link Job} abstraction. */
public class JDFJob extends Job implements Serializable {

    /**
     * Serial identification of the class. It need to be changed only if the class interface is
     * changed.
     */
    private static final long serialVersionUID = 7780896231796955706L;

    private static final Logger logger = Logger.getLogger(JDFJob.class);

    private final String userId;
    private long timestamp;
    private String label;
    private String executionId;

    public JDFJob(String jobId, List<Task> taskList, String userID) {
        super(taskList, jobId);
        this.label = userID + "_job";
        this.userId = userID;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public JDFJob(String jobId, List<Task> taskList, String userID, String executionId) {
        this(jobId, taskList, userID);
        this.executionId = executionId;
    }

    private JDFJob(
            String jobId, List<Task> taskList, String userID, String executionId, long timestamp) {
        this(jobId, taskList, userID, executionId);
        this.timestamp = timestamp;
    }

    public JDFJob(List<Task> taskList, String userID) {
        this(UUID.randomUUID().toString(), taskList, userID);
    }

    public static JDFJob fromJSON(JSONObject job) {
        logger.info("Reading Job from JSON");
        List<Task> tasks = new ArrayList<>();

        JSONArray tasksJSON = job.optJSONArray(JsonKey.TASKS.getKey());
        for (int i = 0; i < tasksJSON.length(); i++) {
            JSONObject taskJSON = tasksJSON.optJSONObject(i);
            Task task = TaskImpl.fromJSON(taskJSON);
            tasks.add(task);
        }

        JDFJob jdfJob =
                new JDFJob(
                        job.optString(JsonKey.JOB_ID.getKey()),
                        tasks,
                        job.optString(JsonKey.USER_ID.getKey()),
                        job.optString(JsonKey.EXECUTION_ID.getKey()),
                        job.optLong(JsonKey.TIMESTAMP.getKey()));
        jdfJob.setLabel(job.optString(JsonKey.LABEL.getKey()));

        try {
            jdfJob.setState(JobState.valueOf(job.optString(JsonKey.STATE.getKey()).toUpperCase()));
        } catch (Exception e) {
            logger.debug("JSON had bad state", e);
        }
        logger.debug(
                "Job read from JSON is from user id: [" + job.optString(JsonKey.USER_ID.getKey()) + "].");
        return jdfJob;
    }

    public String getExecutionId() {
        return this.executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Task getTaskById(String taskId) {
        return this.getTasks().get(taskId);
    }

    public String getUserId() {
        return this.userId;
    }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public JSONObject toJSON() {
        try {
            JSONObject job = new JSONObject();

            job.put(JsonKey.JOB_ID.getKey(), this.getId());
            job.put(JsonKey.USER_ID.getKey(), this.getUserId());
            job.put(JsonKey.TIMESTAMP.getKey(), this.timestamp);
            job.put(JsonKey.LABEL.getKey(), this.getLabel());
            job.put(JsonKey.STATE.getKey(), this.getState().getState());
            job.put(JsonKey.EXECUTION_ID.getKey(), this.executionId);

            JSONArray tasks = new JSONArray();
            Map<String, Task> taskList = this.getTasks();
            for (Entry<String, Task> entry : taskList.entrySet()) {
                tasks.put(entry.getValue().toJSON());
            }
            job.put(JsonKey.TASKS.getKey(), tasks);
            return job;
        } catch (JSONException e) {
            logger.debug("Error while trying to create a JSONObject from JDFJob", e);
            return null;
        }
    }
}

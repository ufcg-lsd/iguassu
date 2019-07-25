package org.fogbowcloud.app.jdfcompiler.job;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * It add the job label, and sched path to the {@link Job} abstraction.
 */
public class JDFJob extends Job implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(JDFJob.class);
    private static final long serialVersionUID = 7780896231796955706L;

    private static final String JSON_HEADER_JOB_ID = "jobId";
    private static final String JSON_HEADER_JOB_ID_ARREBOL = "jobIdArrebol";
    private static final String JSON_HEADER_NAME = "name";
    private static final String JSON_HEADER_UUID = "uuid";
    private static final String JSON_HEADER_STATE = "state";
    private static final String JSON_HEADER_USER_ID = "userId";
    private static final String JSON_HEADER_TASKS = "tasks";
    private static final String JSON_HEADER_TIMESTAMP = "timestamp";

    private final String jobId;
    private final String userId;
    private long timestamp;
    private String label;
    private JDFJobState state;
    private String jobIdArrebol;

    public JDFJob(String jobId, List<Task> taskList, String userID) {
        super(taskList);
        this.label = userID + "_job";
        this.jobId = jobId;
        this.userId = userID;
        this.state = JDFJobState.CREATED;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public JDFJob(String jobId, List<Task> taskList, String userID,
                  String jobIdArrebol) {
        this(jobId, taskList, userID);
        this.jobIdArrebol = jobIdArrebol;
    }

    private JDFJob(String jobId, List<Task> taskList, String userID,
                   String jobIdArrebol, long timestamp) {
        this(jobId, taskList, userID, jobIdArrebol);
        this.timestamp = timestamp;
    }

    public JDFJob(List<Task> taskList, String userID) {
        this(UUID.randomUUID().toString(), taskList, userID);
    }

    public static JDFJob fromJSON(JSONObject job) {
        LOGGER.info("Reading Job from JSON");
        List<Task> tasks = new ArrayList<>();

        JSONArray tasksJSON = job.optJSONArray(JSON_HEADER_TASKS);
        for (int i = 0; i < tasksJSON.length(); i++) {
            JSONObject taskJSON = tasksJSON.optJSONObject(i);
            Task task = TaskImpl.fromJSON(taskJSON);
            tasks.add(task);
        }

        JDFJob jdfJob = new JDFJob(
            job.optString(JSON_HEADER_JOB_ID),
            tasks, job.optString(JSON_HEADER_USER_ID),
            job.optString(JSON_HEADER_JOB_ID_ARREBOL),
            job.optLong(JSON_HEADER_TIMESTAMP)
        );
        jdfJob.setFriendlyName(job.optString(JSON_HEADER_NAME));

        try {
            jdfJob.state = JDFJobState.create(job.optString(JSON_HEADER_STATE));
        } catch (Exception e) {
            LOGGER.debug("JSON had bad state", e);
        }
        LOGGER.debug("Job read from JSON is from userId: " + job.optString(JSON_HEADER_USER_ID));
        return jdfJob;
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobIdArrebol() {
        return jobIdArrebol;
    }

    public void setJobIdArrebol(String jobIdArrebol) {
        this.jobIdArrebol = jobIdArrebol;
    }

    public String getId() {
        return jobId;
    }

    public String getLabel() {
        return this.label;
    }

    public Task getTaskById(String taskId) {
        return this.getTaskList().get(taskId);
    }

    public void setFriendlyName(String name) {
        this.label = name;
    }

    public JDFJobState getState() {
        return this.state;
    }

    @Override
    public void setState(JDFJobState state) {
        this.state = state;
    }

    public void finishCreation() {
        this.state = JDFJobState.CREATED;
    }

    public void failCreation() {
        this.state = JDFJobState.FAILED;
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
            job.put(JSON_HEADER_JOB_ID, this.getId());
            job.put(JSON_HEADER_NAME, this.getLabel());
            job.put(JSON_HEADER_USER_ID, this.getUserId());
            job.put(JSON_HEADER_UUID, this.getUserId());
            job.put(JSON_HEADER_STATE, this.getState().value());
            job.put(JSON_HEADER_JOB_ID_ARREBOL, this.jobIdArrebol);
            job.put(JSON_HEADER_TIMESTAMP, this.timestamp);

            JSONArray tasks = new JSONArray();
            Map<String, Task> taskList = this.getTaskList();
            for (Entry<String, Task> entry : taskList.entrySet()) {
                tasks.put(entry.getValue().toJSON());
            }
            job.put(JSON_HEADER_TASKS, tasks);
            return job;
        } catch (JSONException e) {
            LOGGER.debug("Error while trying to create a JSONObject from JDFJob", e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JDFJob jdfJob = (JDFJob) o;
        return jobId.equals(jdfJob.jobId) && userId.equals(jdfJob.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, userId);
    }

    @Override
    public String toString() {
        return "JDFJob{" +
            "jobId='" + jobId + '\'' +
            ", userId='" + userId + '\'' +
            ", label='" + label + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", state=" + state + '\'' +
            ", jobIdArrebol='" + jobIdArrebol + '\'' +
            '}';
    }
}

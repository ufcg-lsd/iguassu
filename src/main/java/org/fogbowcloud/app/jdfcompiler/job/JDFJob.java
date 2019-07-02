package org.fogbowcloud.app.jdfcompiler.job;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
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
 * It add the job name, job name and sched path to the {@link Job} abstraction.
 */
public class JDFJob extends Job implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(JDFJob.class);
    private static final long serialVersionUID = 7780896231796955706L;

    private static final String JSON_HEADER_JOB_ID = "jobId";
    private static final String JSON_HEADER_JOB_ID_ARREBOL = "jobIdArrebol";
    private static final String JSON_HEADER_NAME = "name";
    private static final String JSON_HEADER_UUID = "uuid";
    private static final String JSON_HEADER_STATE = "state";
    private static final String JSON_HEADER_OWNER = "owner";
    private static final String JSON_HEADER_TASKS = "tasks";

    private final String jobId;
    private final String owner;
    private final String userId;
    private final long timestamp;
    private String name;
    private JDFJobState state;
    private String jobIdArrebol;

    public JDFJob(String jobId, String owner, List<Task> taskList, String userID) {
        super(taskList);
        this.name = "";
        this.jobId = jobId;
        this.owner = owner;
        this.userId = userID;
        this.state = JDFJobState.CREATED;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public JDFJob(String jobId, String owner, List<Task> taskList, String userID,
        String jobIdArrebol) {
        this(jobId, owner, taskList, userID);
        this.jobIdArrebol = jobIdArrebol;
    }

    public JDFJob(String owner, List<Task> taskList, String userID) {
        this(UUID.randomUUID().toString(), owner, taskList, userID);
    }

    // TODO implement JSON_HEADER_JOB_ID_ARREBOL
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
            job.optString(JSON_HEADER_OWNER),
            tasks,
            job.optString(JSON_HEADER_UUID),
            job.optString(JSON_HEADER_JOB_ID_ARREBOL)
        );
        jdfJob.setFriendlyName(job.optString(JSON_HEADER_NAME));
        try {
            jdfJob.state = JDFJobState.create(job.optString(JSON_HEADER_STATE));
        } catch (Exception e) {
            LOGGER.debug("JSON had bad state", e);
        }
        LOGGER.debug("Job read from JSON is from owner: " + job.optString(JSON_HEADER_OWNER));
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

    public String getName() {
        return this.name;
    }

    public String getOwner() {
        return this.owner;
    }

    public Task getTaskById(String taskId) {
        return this.getTaskList().get(taskId);
    }

    public void setFriendlyName(String name) {
        this.name = name;
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

    public long getTimeStamp(){
        return this.timestamp;
    }

    public JSONObject toJSON() {
        try {
            JSONObject job = new JSONObject();
            job.put(JSON_HEADER_JOB_ID, this.getId());
            job.put(JSON_HEADER_NAME, this.getName());
            job.put(JSON_HEADER_OWNER, this.getOwner());
            job.put(JSON_HEADER_UUID, this.getUserId());
            job.put(JSON_HEADER_STATE, this.getState().value());
            job.put(JSON_HEADER_JOB_ID_ARREBOL, this.jobIdArrebol);
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
        return jobId.equals(jdfJob.jobId) && owner.equals(jdfJob.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, owner);
    }

    @Override
    public String toString() {
        return "JDFJob{" +
            "jobId='" + jobId + '\'' +
            ", owner='" + owner + '\'' +
            ", userId='" + userId + '\'' +
            ", name='" + name + '\'' +
            ", state=" + state +
            ", jobIdArrebol='" + jobIdArrebol + '\'' +
            '}';
    }
}

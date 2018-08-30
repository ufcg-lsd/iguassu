package org.fogbowcloud.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * It add the job name, job name and sched path to the {@link Job} abstraction.
 */
public class JDFJob extends Job {

	private static final String JSON_HEADER_JOB_ID = "jobId";
	private static final String JSON_HEADER_NAME = "name";
	private static final String JSON_HEADER_UUID = "uuid";
	private static final String JSON_HEADER_STATE = "state";
	private static final String JSON_HEADER_OWNER = "owner";
	private static final String JSON_HEADER_TASKS = "tasks";

	public enum JDFJobState {
		SUBMITTED("Submitted"),
		FAILED("Failed"),
		CREATED("Created");

		private String desc;

		JDFJobState(String desc) {
			this.desc = desc;
		}

		public String value() {
			return this.desc;
		}

		public static JDFJobState create(String desc) throws Exception{
			for (JDFJobState ts : values()) {
				if(ts.value().equals(desc)){
					return ts;
				}
			}
			throw new Exception("Invalid task state");
		}
	}

	private static final long serialVersionUID = 7780896231796955706L;
	private final String jobId;
	private String name;
	private final String owner;
	private final String userId;
	private JDFJobState state;

	public JDFJob(String owner, List<Task> taskList, String userID) {
		super(taskList);
		this.name = "";
		this.jobId = UUID.randomUUID().toString();
		this.owner = owner;
		this.userId = userID;
		this.state = JDFJobState.SUBMITTED;
	}
	
	public JDFJob(String jobId, String owner, List<Task> taskList, String userID) {
		super(taskList);
		this.name = "";
		this.jobId = jobId;
		this.owner = owner;
		this.userId = userID;
		this.state = JDFJobState.SUBMITTED;
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

	public float completionPercentage() {
		List<Task> tasks = getTasks();
		if (tasks.size() == 0) return 100.0f;
		float completedTasks = 0.0f;
		for (Task task : tasks) {
			if (task.isFinished()) completedTasks++;
		}
		return (float) (100.0*completedTasks/tasks.size());
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

	public void finishCreation() {
		this.state = JDFJobState.CREATED;
	}

	public void failCreation() {
		this.state = JDFJobState.FAILED;
	}

	@Override
	public void finish(Task task) {
		getTaskById(task.getId()).finish();
	}

	@Override
	public void fail(Task task) {
		// TODO Auto-generated method stub
		
	}
	
	public String getUserId() {
		return this.userId;
	}
	
	public JSONObject toJSON() {
		try {
			JSONObject job = new JSONObject();
			job.put(JSON_HEADER_JOB_ID, this.getId());
			job.put(JSON_HEADER_NAME, this.getName());
			job.put(JSON_HEADER_OWNER, this.getOwner());
			job.put(JSON_HEADER_UUID, this.getUserId());
			job.put(JSON_HEADER_STATE, this.getState().value());
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
				job.optString(JSON_HEADER_UUID)
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
	
	@Override
	public boolean equals(Object job2) {
		if (job2 instanceof JDFJob) {
			if (this.toJSON().similar(((JDFJob) job2).toJSON())) {
				return true;
			}
		}
		return false;
	}
}

package org.fogbowcloud.app.jes.arrebol;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jes.exceptions.GetJobException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class ArrebolJobSynchronizer implements JobSynchronizer {

	private static final Logger LOGGER = Logger.getLogger(ArrebolJobSynchronizer.class);

	private final ArrebolRequestsHelper requestsHelper;

	public ArrebolJobSynchronizer(Properties properties) {
		this.requestsHelper = new ArrebolRequestsHelper(properties);
	}

	@Override
	public JDFJob synchronizeJob(JDFJob job) {
		try {
			String arrebolJobJson = this.requestsHelper.getJobJSON(job.getJobIdArrebol());
			LOGGER.debug("JSON Response [" + arrebolJobJson + "]");
			Set<ArrebolTaskState> taskStates = this.getJobTaskStates(arrebolJobJson);
			JDFJobState jobState = getJobState(taskStates);
			LOGGER.debug("Tasks states set [" + taskStates.toString() + "] resuming to State [" + jobState.value() + "]");
			job.setState(jobState);
		} catch (GetJobException e) {
			LOGGER.error(e.getMessage());
		}
		return job;
	}

	public Set<ArrebolTaskState> getJobTaskStates(String arrebolJson) {
		JSONObject jsonObject = new JSONObject(arrebolJson);
		jsonObject = jsonObject.getJSONObject("tasks");
		@SuppressWarnings("unchecked")
		Iterator<String> keys = jsonObject.keys();
		Set<ArrebolTaskState> taskStateList = new HashSet<ArrebolTaskState>();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject taskObject = jsonObject.getJSONObject(key);
			String taskState = taskObject.getString("state");
			LOGGER.debug("State [" + taskState + "] of [" + key + "] Task");
			taskStateList.add(ArrebolTaskState.getTaskStateFromDesc(taskState));
		}
		return taskStateList;
	}
	
	public JDFJobState getJobState(Set<ArrebolTaskState> taskStates) {
		if (taskStates.contains(ArrebolTaskState.FAILED)) {
			return JDFJobState.FAILED;
		} else if (taskStates.contains(ArrebolTaskState.RUNNING)) {
			return JDFJobState.SUBMITTED;
		} else if (taskStates.contains(ArrebolTaskState.FINISHED)) {
			if(taskStates.contains(ArrebolTaskState.PENDING)) {
				return JDFJobState.SUBMITTED;
			} else {
				return JDFJobState.FINISHED;
			}
		} else {
			return JDFJobState.CREATED;
		}
	}

	public enum ArrebolTaskState {
		PENDING("PENDING"), FAILED("FAILED"), FINISHED("FINISHED"), RUNNING("RUNNING"), CLOSED("CLOSED");

		private String desc;

		ArrebolTaskState(String desc) {
			this.desc = desc;
		}

		public String getDesc() {
			return this.desc;
		}

		public static ArrebolTaskState getTaskStateFromDesc(String desc) {
			for (ArrebolTaskState ts : values()) {
				if (ts.getDesc().equals(desc)) {
					return ts;
				}
			}
			return null;
		}
	}
}

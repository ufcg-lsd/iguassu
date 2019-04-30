package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskState;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jes.arrebol.models.*;
import org.fogbowcloud.app.jes.exceptions.GetJobException;
import java.util.*;

public class ArrebolJobSynchronizer implements JobSynchronizer {

	private static final Logger LOGGER = Logger.getLogger(ArrebolJobSynchronizer.class);

	private final ArrebolRequestsHelper requestsHelper;

	public ArrebolJobSynchronizer(Properties properties) {
		this.requestsHelper = new ArrebolRequestsHelper(properties);
	}

	@Override
	public JDFJob synchronizeJob(JDFJob job) {
		try {
			String arrebolJobId = job.getJobIdArrebol();
			if(arrebolJobId != null){
				String arrebolJobJson = this.requestsHelper.getJobJSON(arrebolJobId);
				LOGGER.debug("JSON Response [" + arrebolJobJson + "]");

				Gson gson = new Gson();
				ArrebolJob arrebolJob = gson.fromJson(arrebolJobJson, ArrebolJob.class);
				this.updateJob(job, arrebolJob);
			} else {
				LOGGER.error("ArrebolJobId from Job [" + job.getId() + "] is null.");
			}
		} catch (GetJobException e) {
			LOGGER.error(e.getMessage());
		}
		return job;
	}

	private void updateJob(JDFJob job, ArrebolJob arrebolJob){
		updateTasksState(job, arrebolJob);
		updateJobState(job, arrebolJob.getJobState());
	}

	private void updateTasksState(JDFJob job, ArrebolJob arrebolJob){
		Map<String, ArrebolTask> arrebolTasks = arrebolJob.getTasks();
		LOGGER.info("Updating tasks state from job [" + job.getId() + "].");
		for(Task task : job.getTaskList().values()){
			ArrebolTask arrebolTask = arrebolTasks.get(task.getId());
			ArrebolTaskState arrebolTaskState = arrebolTask.getState();
			TaskState taskState = getTaskState(arrebolTaskState);
			task.setState(taskState);
			LOGGER.debug("Updated task [" + task.getId() + "] to state " + taskState.toString());
		}
	}

	private void updateJobState(JDFJob job, ArrebolJobState arrebolJobState){
		JDFJobState jdfJobState = this.getJobState(arrebolJobState);
		job.setState(jdfJobState);
		LOGGER.info("Updated job [" + job.getId() + "] to state " + jdfJobState.toString());
	}

	private TaskState getTaskState(ArrebolTaskState arrebolTaskState){
		if(arrebolTaskState.equals(ArrebolTaskState.FAILED)){
			return TaskState.FAILED;
		} else if(arrebolTaskState.equals(ArrebolTaskState.RUNNING)){
			return TaskState.RUNNING;
		} else if(arrebolTaskState.equals(ArrebolTaskState.FINISHED)){
			return TaskState.FINISHED;
		} else if(arrebolTaskState.equals(ArrebolTaskState.PENDING)){
			return TaskState.READY;
		} else if(arrebolTaskState.equals(ArrebolTaskState.CLOSED)){
			return TaskState.COMPLETED;
		}
		return null;
	}

	private JDFJobState getJobState(ArrebolJobState arrebolJobState){
		if(arrebolJobState.equals(ArrebolJobState.FAILED)){
			return JDFJobState.FAILED;
		} else if(arrebolJobState.equals(ArrebolJobState.FINISHED)){
			return JDFJobState.FINISHED;
		} else if(arrebolJobState.equals(ArrebolJobState.SUBMITTED)){
			return JDFJobState.SUBMITTED;
		} else if(arrebolJobState.equals(ArrebolJobState.RUNNING)){
			return JDFJobState.SUBMITTED;
		} else if(arrebolJobState.equals(ArrebolJobState.READY)){
			return JDFJobState.CREATED;
		}
		return null;
	}
}

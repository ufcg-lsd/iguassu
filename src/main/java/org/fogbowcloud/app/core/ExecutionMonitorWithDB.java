package org.fogbowcloud.app.core;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.models.JDFJob;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskState;

public class ExecutionMonitorWithDB implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ExecutionMonitorWithDB.class);

	private IguassuController iguassuController;
	private ExecutorService executorService;
    private JobDataStore db;

	ExecutionMonitorWithDB(IguassuController iguassuController, JobDataStore dataStore) {
		this(iguassuController, Executors.newFixedThreadPool(3), dataStore);
	}

	ExecutionMonitorWithDB(IguassuController iguassuController, ExecutorService executorService, JobDataStore db) {
		this.iguassuController = iguassuController;
		if (executorService == null) {
			this.executorService = Executors.newFixedThreadPool(3);
		} else {
			this.executorService = executorService;
		}
        this.db = db;
	}

	@Override
	public void run() {
		LOGGER.info("Submitting monitoring tasks.");
		ArrayList<JDFJob> jobMap = (ArrayList<JDFJob>) db.getAll();
		for (JDFJob aJob : jobMap) {
			LOGGER.info("Starting monitoring of job " + aJob.getName() + "[" + aJob.getId() + "].");
			int count = 0;
			for (Task task : aJob.getTasks()) {
				if (!task.isFinished()) {
					count++;
					LOGGER.info("Task: " + task.getId() +" is being treated");
					TaskState taskState = iguassuController.getTaskState(task.getId());
					LOGGER.info("Process " + task.getId() + " has state " + taskState.getDesc());
					executorService.submit(new TaskExecutionChecker(task));
				}
			}
			if (count == 0) {
				LOGGER.info("Job has no active tasks.");
			}
		}
	}

	class TaskExecutionChecker implements Runnable {

		private Task task;

		TaskExecutionChecker(Task task) {
			this.task = task;
		}

		@Override
		public void run() {
			TaskState state = iguassuController.getTaskState(task.getId());

			if (TaskState.COMPLETED.equals(state)) {
				iguassuController.moveTaskToFinished(task);
				LOGGER.info("The task " + this.task.getId() + " has been completed!");
			}
		}
	}
}

package org.fogbowcloud.app;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fogbowcloud.app.datastore.JobDataStore;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;

public class ExecutionMonitorWithDB implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionMonitorWithDB.class);

	private ArrebolController arrebolController;
	private ExecutorService service;
    private JobDataStore db;

	ExecutionMonitorWithDB(ArrebolController arrebolController,
                           JobDataStore dataStore) {
		this(arrebolController, Executors.newFixedThreadPool(3), dataStore);
	}

	ExecutionMonitorWithDB(ArrebolController arrebolController,
                           ExecutorService service,
                           JobDataStore db) {
		this.arrebolController = arrebolController;
		if (service == null) {
			this.service = Executors.newFixedThreadPool(3);
		} else {
			this.service = service;
		}
        this.db = db;
	}

	@Override
	public void run() {
		LOGGER.debug("Submitting monitoring tasks");
		ArrayList<JDFJob> jobMap = (ArrayList<JDFJob>) db.getAll();
		for (JDFJob aJob : jobMap) {
			LOGGER.debug("Starting monitoring of job " + aJob.getName() + "[" + aJob.getId() + "].");
			int count = 0;
			for (Task task : aJob.getTasks()) {
				if (!task.isFinished()) {
					count++;
					LOGGER.debug("Task: " + task +" is being treated");
					TaskState taskState = arrebolController.getTaskState(task.getId());
					LOGGER.debug("Process " + task.getId() + " has state " + taskState.getDesc());
					service.submit(new TaskExecutionChecker(task));
				}
			}
			if (count == 0) {
				LOGGER.debug("Job has no active tasks.");
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
			TaskState state = arrebolController.getTaskState(task.getId());

			if (TaskState.COMPLETED.equals(state)) {
				arrebolController.moveTaskToFinished(task);
			}
		}
	}
}

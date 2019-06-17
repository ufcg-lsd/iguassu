package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.command.CommandState;
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

    //TODO Review method name
    @Override
    public JDFJob synchronizeJob(JDFJob job) {
        try {
            String arrebolJobId = job.getJobIdArrebol();
            if (arrebolJobId != null) {
                String arrebolJobJson = this.requestsHelper.getJobJSON(arrebolJobId);
                LOGGER.debug("JSON Response [" + arrebolJobJson + "]");

                Gson gson = new Gson();
                ArrebolJob arrebolJob = gson.fromJson(arrebolJobJson, ArrebolJob.class);
                this.updateJob(job, arrebolJob);
            } else {
                LOGGER.info("ArrebolJobId from Job [" + job.getId() + "] is null.");
            }
        } catch (GetJobException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return job;
    }

    private void updateJob(JDFJob job, ArrebolJob arrebolJob) {
        updateCommandsState(job.getTaskList(), arrebolJob.getTasks());
        updateTasksState(job.getTaskList(), arrebolJob.getTasks());
        LOGGER.info("Updated tasks state from job [" + job.getId() + "].");
        updateJobState(job, arrebolJob.getJobState());
    }

    private void updateCommandsState(Map<String, Task> iguassuTasks, List<ArrebolTask> arrebolTasks) {
        // To Review This produce some collateral effect, attempt for this.
        for (ArrebolTask arrebolTask : arrebolTasks) {
            String taskIguassuId = arrebolTask.getId();
            Task iguassuTask = iguassuTasks.get(taskIguassuId);

            if (iguassuTask != null) {
                updateCommands(arrebolTask, iguassuTask);
            }
        }
    }

    private void updateCommands(ArrebolTask arrebolTask, Task iguassuTask) {
        List<ArrebolCommand> arrebolCommands = arrebolTask.getTaskSpec().getCommands();
        for(int i = 0; i < arrebolCommands.size(); i++){
            ArrebolCommand arrebolCommand = arrebolCommands.get(i);
            Command command = iguassuTask.getAllCommands().get(i);
            CommandState commandState = getCommandState(arrebolCommand.getState());
            command.setState(commandState);
        }
    }

    private void updateTasksState(Map<String, Task> tasks, List<ArrebolTask> arrebolTasks) {

        for (ArrebolTask arrebolTask : arrebolTasks) {
            String taskId = arrebolTask.getTaskSpec().getId();
            Task task = tasks.get(taskId);

            ArrebolTaskState arrebolTaskState = arrebolTask.getState();
            TaskState taskState = getTaskState(arrebolTaskState);
            task.setState(taskState);
            LOGGER.debug("Updated task [" + task.getId() + "] to state " + taskState.toString());
        }
    }

    private void updateJobState(JDFJob job, ArrebolJobState arrebolJobState) {
        JDFJobState jdfJobState = this.getJobState(arrebolJobState);

        if (jdfJobState.equals(JDFJobState.RUNNING) && tasksFinished(job)) {
            jdfJobState = JDFJobState.FINISHED;
        }

        job.setState(jdfJobState);
        LOGGER.info("Updated job [" + job.getId() + "] to state " + jdfJobState.toString());
    }

    private boolean tasksFinished(JDFJob job) {
        int tasksFinished = 0;

        for (Task task : job.getTasks()) {
            if (task.getState().equals(TaskState.FINISHED)) tasksFinished++;
        }

        return job.getTasks().size() == tasksFinished;
    }

    private CommandState getCommandState(ArrebolCommandState arrebolCommandState) {
        switch (arrebolCommandState) {
            case RUNNING:
            case UNSTARTED:
                return CommandState.RUNNING;
            case FINISHED:
                return CommandState.FINISHED;
            case FAILED:
                return CommandState.FAILED;
            default:
                return null;
        }
    }

    private TaskState getTaskState(ArrebolTaskState arrebolTaskState) {
        switch (arrebolTaskState) {
            case RUNNING:
                return TaskState.RUNNING;
            case FINISHED:
                return TaskState.FINISHED;
            case PENDING:
                return TaskState.PENDING;
            case FAILED:
                return TaskState.FAILED;
            default:
                return null;
        }
    }

    private JDFJobState getJobState(ArrebolJobState arrebolJobState) {
        switch (arrebolJobState) {
            case SUBMITTED:
                return JDFJobState.SUBMITTED;
            case READY:
                return JDFJobState.RUNNING;
            case RUNNING:
                return JDFJobState.RUNNING;
            case FAILED:
                return JDFJobState.FAILED;
            default:
                return null;
        }
    }


}

package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.command.CommandState;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskState;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JobState;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolCommand;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolCommandState;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolJob;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolTask;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolTaskState;
import org.fogbowcloud.app.jes.arrebol.models.ExecutionState;
import org.fogbowcloud.app.jes.exceptions.JobStatusException;

public class ArrebolSynchronizer implements Synchronizer<JDFJob> {

    private static final Logger logger = Logger.getLogger(ArrebolSynchronizer.class);

    private final ArrebolRequestsHelper requestsHelper;

    public ArrebolSynchronizer(Properties properties) {
        this.requestsHelper = new ArrebolRequestsHelper(properties);
    }

    @Override
    public JDFJob sync(JDFJob job) {

        final String executionId = job.getExecutionId();
        if (Objects.nonNull(executionId) && !executionId.trim().isEmpty()) {
            try {
                String jobExecutionJson = this.requestsHelper.statusArrebolJob(executionId);
                logger.debug("JSON Response [" + jobExecutionJson + "]");
                Gson gson = new Gson();
                ArrebolJob arrebolJob = gson.fromJson(jobExecutionJson, ArrebolJob.class);
                this.updateJob(job, arrebolJob);
            } catch (Exception e) {
                throw new JobStatusException(e.getMessage());
            }
        } else {
            logger.debug("Execution identifier from Job [" + job.getId() + "] is null.");
        }

        return job;
    }

    private void updateJob(JDFJob job, ArrebolJob arrebolJob) {
        updateTasks(job.getTasks(), arrebolJob.getTasks());
        logger.info("Updated tasks state from job [" + job.getId() + "].");
        updateJobState(job, arrebolJob.getExecutionState());
    }

    private void updateTasks(Map<String, Task> iguassuTasks, List<ArrebolTask> arrebolTasks) {
        for (ArrebolTask arrebolTask : arrebolTasks) {
            final String iguassuTaskId = arrebolTask.getTaskSpec().getId();
            final Task iguassuTask = iguassuTasks.get(iguassuTaskId);

            if (iguassuTask != null) {
                updateTaskCommands(arrebolTask, iguassuTask);

                final ArrebolTaskState arrebolTaskState = arrebolTask.getState();
                final TaskState taskState = getTaskState(arrebolTaskState);
                if (Objects.nonNull(taskState)) {
                    iguassuTask.setState(taskState);
                    logger.debug(
                            "Updated task ["
                                    + iguassuTask.getId()
                                    + "] to state "
                                    + taskState.toString());
                }
            }
        }
    }

    private void updateTaskCommands(ArrebolTask arrebolTask, Task iguassuTask) {
        List<ArrebolCommand> arrebolCommands = arrebolTask.getTaskSpec().getCommands();
        for (int i = 0; i < arrebolCommands.size(); i++) {
            ArrebolCommand arrebolCmd = arrebolCommands.get(i);
            Command command = iguassuTask.getAllCommands().get(i);
            CommandState commandState = getCommandState(arrebolCmd.getState());
            command.setState(commandState);
            command.setExitCode(arrebolCmd.getExitcode());
        }
    }

    private void updateJobState(JDFJob job, ExecutionState executionState) {
        JobState jobState = this.getJobState(executionState);

        if (jobState != null) {
            job.setState(jobState);
            logger.info("Updated job [" + job.getId() + "] to state " + jobState.toString());
        }
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

    private JobState getJobState(ExecutionState executionState) {
        switch (executionState) {
            case SUBMITTED:
                return JobState.SUBMITTED;
            case QUEUED:
                return JobState.QUEUED;
            case RUNNING:
                return JobState.RUNNING;
            case FAILED:
                return JobState.FAILED;
            case FINISHED:
                return JobState.FINISHED;
            default:
                return null;
        }
    }
}

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
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolCommand;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolCommandState;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolJob;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolJobState;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolTask;
import org.fogbowcloud.app.jes.arrebol.models.ArrebolTaskState;
import org.fogbowcloud.app.jes.exceptions.GetJobException;

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
            if (Objects.nonNull(arrebolJobId) && !arrebolJobId.trim().isEmpty()) {
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
        updateTasks(job.getTaskList(), arrebolJob.getTasks());
        LOGGER.info("Updated tasks state from job [" + job.getId() + "].");
        updateJobState(job, arrebolJob.getJobState());
    }

    private void updateTasks(Map<String, Task> iguassuTasks, List<ArrebolTask> arrebolTasks) {
        // This produce some collateral effect, attempt for this.
        for (ArrebolTask arrebolTask : arrebolTasks) {
            String taskIguassuId = arrebolTask.getTaskSpec().getId();
            Task iguassuTask = iguassuTasks.get(taskIguassuId);

            if (iguassuTask != null) {
                updateTaskCommands(arrebolTask, iguassuTask);

                ArrebolTaskState arrebolTaskState = arrebolTask.getState();
                TaskState taskState = getTaskState(arrebolTaskState);
                iguassuTask.setState(taskState);
                LOGGER.debug(
                    "Updated task [" + iguassuTask.getId() + "] to state " + taskState.toString());
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

    private void updateJobState(JDFJob job, ArrebolJobState arrebolJobState) {
        JDFJobState jdfJobState = this.getJobState(arrebolJobState);

        if (jdfJobState != null) {
            job.setState(jdfJobState);
            LOGGER.info("Updated job [" + job.getId() + "] to state " + jdfJobState.toString());
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

    private JDFJobState getJobState(ArrebolJobState arrebolJobState) {
        switch (arrebolJobState) {
            case SUBMITTED:
                return JDFJobState.SUBMITTED;
            case QUEUED:
                return JDFJobState.QUEUED;
            case RUNNING:
                return JDFJobState.RUNNING;
            case FAILED:
                return JDFJobState.FAILED;
            case FINISHED:
                return JDFJobState.FINISHED;
            default:
                return null;
        }
    }
}

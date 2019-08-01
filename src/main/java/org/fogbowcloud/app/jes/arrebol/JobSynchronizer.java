package org.fogbowcloud.app.jes.arrebol;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.command.CommandState;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskState;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JobState;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.arrebol.models.*;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;
import org.fogbowcloud.app.jes.exceptions.JobExecStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Thread.sleep;

/** Sync local job state with it execution. */
public class JobSynchronizer implements Synchronizer<JDFJob> {

    private static final Logger logger = Logger.getLogger(JobSynchronizer.class);

    private final JobExecutionService jobExecutionService;

    public JobSynchronizer(JobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    @Override
    public JDFJob sync(JDFJob job) {

        final String executionId = job.getExecutionId();
        if (Objects.nonNull(executionId) && !executionId.trim().isEmpty()) {
            JobExecArrebol jobExecArrebol = null;
            while (Objects.isNull(jobExecArrebol)) {
                try {
                    jobExecArrebol = this.jobExecutionService.status(executionId);
                } catch (ArrebolConnectException ace) {
                    logger.debug(
                            "Error to get status for execution ["
                                    + executionId
                                    + "] with message + "
                                    + ace.getMessage());
                    try {
                        final long waitTime = 2000;
                        sleep(waitTime);
                    } catch (InterruptedException e) {
                        logger.debug("Thread can't sleep cause by: " + e.getMessage());
                    }

                    jobExecArrebol = null;
                } catch (RuntimeException e) {
                    throw new JobExecStatusException(e.getMessage());
                }
            }
            this.updateJob(job, jobExecArrebol);
        } else {
            logger.debug("Execution identifier from Job [" + job.getId() + "] is null.");
        }

        return job;
    }

    private void updateJob(JDFJob job, JobExecArrebol jobExecArrebol) {
        updateTasks(job.getTasks(), jobExecArrebol.getTasks());
        logger.info("Updated tasks state from job [" + job.getId() + "].");
        updateJobState(job, jobExecArrebol.getState());
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
package org.fogbowcloud.app.jes.arrebol.dtos;

import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.task.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Data Transfer Object of the Arrebol Job Execution.
 */
public class ArrebolExecutionDTO implements Serializable {

    /**
     * Serial identification of the class. It need to be changed only if the class interface is
     * changed.
     */
    private static final long serialVersionUID = 1L;

    private String label;
    private List<TaskSpecDTO> tasksSpecs;

    public ArrebolExecutionDTO(Job job) {
        this.tasksSpecs = new ArrayList<>();
        this.label = job.getLabel();
        populateTaskSpec(job);
    }

    private void populateTaskSpec(Job job) {
        List<Task> taskList = job.getTasks();
        for (Task task : taskList) {
            this.tasksSpecs.add(
                    new TaskSpecDTO(
                            task.getId(),
                            task.getRequirements(),
                            task.getAllCommandsInStr(),
                            task.getMetadata()));
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<TaskSpecDTO> getTasksSpecs() {
        return tasksSpecs;
    }

    public void setTasksSpecs(List<TaskSpecDTO> tasksSpecs) {
        this.tasksSpecs = tasksSpecs;
    }
}

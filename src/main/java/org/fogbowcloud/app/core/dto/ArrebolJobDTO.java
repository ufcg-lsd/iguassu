package org.fogbowcloud.app.core.dto;

import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArrebolJobDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String label;
    private List<TaskSpecDTO> tasksSpecs;

    public ArrebolJobDTO(JDFJob job) {
        this.tasksSpecs = new ArrayList<>();
        this.label = job.getName();
        populateTaskSpec(job);
    }

    private void populateTaskSpec(JDFJob job) {
        List<Task> taskList = job.getTasks();
        for (Task task : taskList) {
            this.tasksSpecs.add(new TaskSpecDTO(task.getId(), task.getSpecification(),
                    task.getAllCommandsInStr(), task.getAllMetadata()));
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

package org.fogbowcloud.app.core.dto;

import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JobDTO implements Serializable {
    private String label;
    private List<TaskSpecDTO> tasksSpecs;

    public JobDTO(JDFJob job) {
        this.tasksSpecs = new ArrayList<>();
        this.label = job.getName();
        populateTaskSpec(job);
    }

    private void populateTaskSpec(JDFJob job) {
        List<Task> taskList = job.getTasks();
        for (int i = 0; i < job.getTasks().size(); i++ ) {
            List<String> commands = taskList.get(i).getAllCommandsInStr();
            this.tasksSpecs.add(
                    new TaskSpecDTO(
                            taskList.get(i).getSpecification(),
                            taskList.get(i).getAllCommandsInStr(),
                            taskList.get(i).getAllMetadata()
                    )
            );
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

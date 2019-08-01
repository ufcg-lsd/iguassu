package org.fogbowcloud.app.jdfcompiler.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.fogbowcloud.app.core.task.Task;

public abstract class Job implements Serializable {

    private static final long serialVersionUID = -6111900503095749695L;

    private final String jobId;
    private Map<String, Task> tasks;
    private JobState state;
    private final ReentrantReadWriteLock taskReadyLock;

    public Job(List<Task> tasks, String jobId) {
        this.jobId = jobId;
        this.state = JobState.CREATED;
        this.tasks = new HashMap<>();
        this.taskReadyLock = new ReentrantReadWriteLock();
        addTasks(tasks);
    }

    public void addTask(Task task) {
        taskReadyLock.writeLock().lock();
        try {
            getTasks().put(task.getId(), task);
        } finally {
            taskReadyLock.writeLock().unlock();
        }
    }

    private void addTasks(List<Task> tasks) {
        for (Task task : tasks) {
            addTask(task);
        }
    }

    public JobState getState() {
        return this.state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public void finishCreation() {
        this.state = JobState.CREATED;
    }

    public void failCreation() {
        this.state = JobState.FAILED;
    }

    public String getId() {
        return this.jobId;
    }

    public List<Task> getTasksAsList() {
        return new ArrayList<>(tasks.values());
    }

    public Map<String, Task> getTasks() {
        return this.tasks;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", state=" + state +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (!jobId.equals(job.jobId)) return false;
        if (!tasks.equals(job.tasks)) return false;
        if (state != job.state) return false;
        return taskReadyLock.equals(job.taskReadyLock);
    }

    @Override
    public int hashCode() {
        int result = jobId.hashCode();
        result = 31 * result + tasks.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + taskReadyLock.hashCode();
        return result;
    }
}

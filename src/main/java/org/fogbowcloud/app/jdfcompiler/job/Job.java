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

    private Map<String, Task> taskList;
    private final ReentrantReadWriteLock taskReadyLock;

    public Job(List<Task> tasks) {
        this.taskList = new HashMap<>();
        this.taskReadyLock = new ReentrantReadWriteLock();
        addTasks(tasks);
    }

    public void addTask(Task task) {
        taskReadyLock.writeLock().lock();
        try {
            getTaskList().put(task.getId(), task);
        } finally {
            taskReadyLock.writeLock().unlock();
        }
    }

    private void addTasks(List<Task> tasks) {
        for (Task task : tasks) {
            addTask(task);
        }
    }

    public List<Task> getTasks() {
        return new ArrayList<>(taskList.values());
    }

    public abstract void setState(JDFJobState state);

    public abstract String getId();

    public Map<String, Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(Map<String, Task> taskList) {
        this.taskList = taskList;
    }
}

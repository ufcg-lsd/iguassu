package org.fogbowcloud.app.core.datastore.managers;

import org.fogbowcloud.app.core.datastore.repositories.JobRepository;
import org.fogbowcloud.app.core.datastore.repositories.TaskRepository;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.task.Task;

import java.util.List;

public class JobDBManager {
    private static JobDBManager instance;

    private JobRepository jobRepository;
    private TaskRepository taskRepository;

    private JobDBManager() { }

    public synchronized static JobDBManager getInstance() {
        if (instance == null) {
            instance = new JobDBManager();
        }
        return instance;
    }

    public void save(Task task) { this.taskRepository.save(task); }

    public void save(Job job) {
        this.jobRepository.save(job);
    }

    public Job findOne(String id) {
        return this.jobRepository.findById(id).isPresent() ? this.jobRepository.findById(id).get() : null;
    }

    public List<Job> findAll() {
        return this.jobRepository.findAll();
    }

    public void update(Job job) {
        this.jobRepository.save(job);
    }

    public List<Job> findByUserId(Long ownerId) {
        return this.jobRepository.findAllByOwnerId(ownerId);
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}

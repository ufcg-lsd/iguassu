package org.fogbowcloud.app.core.datastore.managers;

import org.fogbowcloud.app.core.datastore.repositories.JobRepository;
import org.fogbowcloud.app.core.models.job.Job;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class JobDBManager {
    private static JobDBManager instance;

    @Autowired
    private JobRepository jobRepository;

    private JobDBManager() {
    }

    public synchronized static JobDBManager getInstance() {
        if (instance == null) {
            instance = new JobDBManager();
        }
        return instance;
    }

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
        this.jobRepository.deleteById(job.getId());
        this.jobRepository.save(job);
    }

    public List<Job> findByUserId(long userId) {
        return this.jobRepository.findAllByOwnerId(userId);
    }

    public void delete(String id) {
        this.jobRepository.deleteById(id);
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }
}

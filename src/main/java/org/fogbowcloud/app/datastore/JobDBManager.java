package org.fogbowcloud.app.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.datastore.repositories.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class JobDBManager implements DBManager<Job> {

    private static final Logger logger = Logger.getLogger(JobDBManager.class);

    private static JobDBManager instance;

    @Autowired
    private JobRepository jobRepository;

    private JobDBManager() {}

    public synchronized static JobDBManager getInstance() {
        if (instance == null) {
            instance = new JobDBManager();
        }
        return instance;
    }

    @Override
    public void save(Job job) {
        this.jobRepository.save(job);
    }

    @Override
    public Job findOne(String id) {
        return this.jobRepository.findOne(id);
    }

    public List<Job> findAll() {
        return this.jobRepository.findAll();
    }

    @Override
    public void update(Job job) {
        this.jobRepository.delete(job.getId());
        this.jobRepository.save(job);
    }

    @Override
    public void delete(String id) {
        this.jobRepository.delete(id);
    }
}

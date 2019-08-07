package org.fogbowcloud.app.datastore.managers;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.datastore.DBManager;
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
    public Job findOne(long id) {
        return this.jobRepository.findById(id).isPresent() ? this.jobRepository.findById(id).get() : null;
    }

    public List<Job> findAll() {
        return this.jobRepository.findAll();
    }

    @Override
    public void update(Job job) {
        this.jobRepository.deleteById(job.getId());
        this.jobRepository.save(job);
    }

    @Override
    public void delete(long id) {
        this.jobRepository.deleteById(id);
    }
}

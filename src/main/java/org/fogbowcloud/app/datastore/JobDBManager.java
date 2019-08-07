package org.fogbowcloud.app.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.datastore.repositories.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;

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

    }

    @Override
    public Job retrieveById(String id) {
        return null;
    }

    @Override
    public void update(Job job) {

    }
}

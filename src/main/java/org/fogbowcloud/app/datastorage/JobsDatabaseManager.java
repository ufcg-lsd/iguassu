package org.fogbowcloud.app.datastorage;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.job.Job;
import org.springframework.beans.factory.annotation.Autowired;

public class JobsDatabaseManager implements DatabaseManager<Job> {

    private static final Logger logger = Logger.getLogger(JobsDatabaseManager.class);

    private static JobsDatabaseManager instance;

    @Autowired
    private JobRepository jobRepository;

    private JobsDatabaseManager() {}

    public synchronized static JobsDatabaseManager getInstance() {
        if (instance == null) {
            instance = new JobsDatabaseManager();
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

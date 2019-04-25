package org.fogbowcloud.app.jdfcompiler.job;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.jes.JobExecutionSystem;
import org.fogbowcloud.app.core.datastore.JobDataStore;

import java.util.Properties;

public class AsyncJobBuilder implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(AsyncJobBuilder.class);
    private JDFJob job;
    private String jdfFilePath;
    private Properties properties;
    private JobDataStore db;
    private JobSpecification jobSpec;
    private JDFJobBuilder jdfJobBuilder;
    private String userName;
    private String externalOAuthToken;

    public AsyncJobBuilder(JDFJob job, String jdfFilePath, Properties properties, JobDataStore db,
             JobSpecification jobSpec, String userName, String externalOAuthToken) {
        this.job = job;
        this.jdfFilePath = jdfFilePath;
        this.properties = properties;
        this.db = db;
        this.jobSpec = jobSpec;
        this.jdfJobBuilder = new JDFJobBuilder(this.properties);
        this.userName = userName;
        this.externalOAuthToken = externalOAuthToken;
    }

    @Override
    public void run() {
        try {
            this.jdfJobBuilder.createJobFromJDFFile(this.job, this.jdfFilePath, this.jobSpec,
                    this.userName, this.externalOAuthToken);

            LOGGER.info("Job [" + job.getId() + "] was built with success at time: " + System.currentTimeMillis());

            this.job.finishCreation();
        } catch (Exception e) {
            LOGGER.error("Failed to build [" + job.getId() + "] : at time: " + System.currentTimeMillis(), e);
            this.job.failCreation();
        }
        this.db.update(job);
    }
}
package org.fogbowcloud.app.jdfcompiler.job;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.arrebol.ArrebolFacade;
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
    private ArrebolFacade arrebolFacade;

    public AsyncJobBuilder(JDFJob job, String jdfFilePath, Properties properties, JobDataStore db,
             JobSpecification jobSpec, String userName, String externalOAuthToken, ArrebolFacade arrebolFacade) {
        this.job = job;
        this.jdfFilePath = jdfFilePath;
        this.properties = properties;
        this.db = db;
        this.jobSpec = jobSpec;
        this.jdfJobBuilder = new JDFJobBuilder(this.properties);
        this.userName = userName;
        this.externalOAuthToken = externalOAuthToken;
        this.arrebolFacade = arrebolFacade;
    }

    @Override
    public void run() {
        try {

            this.jdfJobBuilder.createJobFromJDFFile(this.job, this.jdfFilePath, this.jobSpec,
                    this.userName, this.externalOAuthToken);

            this.arrebolFacade.executeJob(this.job);

            LOGGER.info("Submitted " + job.getId() + " to blowout at time: " + System.currentTimeMillis());

            this.job.finishCreation();
        } catch (Exception e) {
            LOGGER.error("Failed to Submit " + job.getId() + " to blowout at time: " + System.currentTimeMillis(), e);
            this.job.failCreation();
        }
        this.db.update(job);
    }
}
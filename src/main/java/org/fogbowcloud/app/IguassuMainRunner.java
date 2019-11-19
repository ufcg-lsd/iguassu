package org.fogbowcloud.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.constants.AppConstant;
import org.fogbowcloud.app.core.constants.ExitCode;
import org.fogbowcloud.app.core.datastore.managers.JobDBManager;
import org.fogbowcloud.app.core.datastore.managers.QueueDBManager;
import org.fogbowcloud.app.core.datastore.managers.UserDBManager;
import org.fogbowcloud.app.core.datastore.repositories.JobRepository;
import org.fogbowcloud.app.core.datastore.repositories.QueueRepository;
import org.fogbowcloud.app.core.datastore.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class IguassuMainRunner implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(IguassuMainRunner.class);

    @Autowired
    Properties properties;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    QueueRepository queueRepository;


    @Override
    public void run(String... args) {
        JobDBManager.getInstance().setJobRepository(jobRepository);
        UserDBManager.getInstance().setUserRepository(userRepository);
        QueueDBManager.getInstance().setQueueRepository(queueRepository);
        QueueDBManager.getInstance().init();

        logger.info("Running " + IguassuMainRunner.class.getName());
        loadProperties();

        try {
            ApplicationFacade.getInstance().init(this.properties);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

    }

    private void loadProperties() {
        String confFilePath = System.getProperty(IguassuApplication.CONF_FILE_PROPERTY);

        try {
            if (Objects.isNull(confFilePath)) {
                this.properties.load(IguassuApplication.class.getClassLoader()
                    .getResourceAsStream(AppConstant.CONF_FILE_NAME));
            } else {
                loadProperties(confFilePath);
                logger.info("Configuration file " + confFilePath + " was loaded with success.");
            }
        } catch (Exception e) {
            logger.info("Configuration file was not founded or not loaded with success.");
            System.exit(ExitCode.FAIL.getCode());
        }
    }

    private void loadProperties(String fileName) throws Exception {
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(fileName);
            this.properties.load(fileInputStream);
        } catch (FileNotFoundException fnfe) {
            throw new Exception(String.format("Property file %s not found.", fileName), fnfe);
        } catch (IOException ioe) {
            throw new Exception(ioe.getMessage(), ioe);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }
}

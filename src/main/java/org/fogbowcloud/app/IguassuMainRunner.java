package org.fogbowcloud.app;

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.constants.ConfProperties;
import org.fogbowcloud.app.utils.ConfValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;

public class IguassuMainRunner implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(IguassuMainRunner.class);
    private static String CONF_FILE_PATH = ConfProperties.IGUASSU_CONF_FILE;

    @Lazy
    @Autowired
    IguassuController iguassuController;

    @Autowired
    private Properties properties;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Running " + IguassuMainRunner.class.getName());
        if (args.length > 0) {
            CONF_FILE_PATH = args[0];
            LOGGER.info("Configuration file found in path " + CONF_FILE_PATH + ".");

        } else {
            LOGGER.info("Configuration file found in default path " + CONF_FILE_PATH + ".");
        }
        loadProperties(CONF_FILE_PATH);
    }

    private void loadProperties(String iguassuConfFilePath) {
        try {
            properties.load(new FileInputStream(iguassuConfFilePath));
            LOGGER.info("Configuration file " + iguassuConfFilePath + " was loaded with success.");
            ConfValidator.validate(this.properties);
        } catch (Exception e) {
            LOGGER.info("Configuration file was not founded or not loaded with success.");
            System.exit(1);
        }
    }
}
